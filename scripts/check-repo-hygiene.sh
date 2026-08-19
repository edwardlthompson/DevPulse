#!/usr/bin/env bash
# Orchestrate repo hygiene checks (CI and local entrypoint)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ERRORS=0

run_step() {
  if ! "$@"; then
    ERRORS=$((ERRORS + 1))
  fi
}

run_step bash scripts/check-tracked-artifacts.sh
run_step bash scripts/check-large-tracked-files.sh

REQUIRED_IGNORES=(
  node_modules/
  dist/
  .env
  __pycache__/
  coverage/
)
if [ -f .gitignore ]; then
  for entry in "${REQUIRED_IGNORES[@]}"; do
    if ! grep -qF "$entry" .gitignore; then
      echo "MISSING .gitignore entry: $entry"
      ERRORS=$((ERRORS + 1))
    fi
  done
else
  echo "MISSING: .gitignore"
  ERRORS=$((ERRORS + 1))
fi

for f in .gitattributes .editorconfig; do
  if [ ! -f "$f" ]; then
    echo "MISSING: $f"
    ERRORS=$((ERRORS + 1))
  fi
done

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS repo hygiene check(s) failed"
  exit 1
fi

echo "Repo hygiene checks passed"
