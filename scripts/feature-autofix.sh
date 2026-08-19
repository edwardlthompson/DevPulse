#!/usr/bin/env bash
# Mechanical auto-fix (format/lint fixers) within optional --paths scope.
# Usage: scripts/feature-autofix.sh [--dry-run] [--paths dir1,dir2]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

DRY=false
PATHS=""
FIX_FAILED=0
while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run) DRY=true; shift ;;
    --paths=*) PATHS="${1#*=}"; shift ;;
    --paths) PATHS="${2:-}"; shift 2 ;;
    *) shift ;;
  esac
done

if [ -f .cursor/stack-selection.json ]; then
  STACK="$($PY -c "import json; print(json.load(open('.cursor/stack-selection.json')).get('stack','multi'))" 2>/dev/null || echo multi)"
fi
STACK="${STACK:-multi}"

run_fix() {
  local desc="$1"
  shift
  if [ "$DRY" = true ]; then
    echo "[dry-run] $desc: $*"
    return 0
  fi
  echo "autofix: $desc"
  if "$@"; then
    return 0
  fi
  FIX_FAILED=1
  return 1
}

should_run() {
  local s="$1"
  [ "$STACK" = "multi" ] || [ "$STACK" = "none" ] || [ "$STACK" = "$s" ]
}

if should_run python && [ -f examples/python/pyproject.toml ] && command -v uv >/dev/null 2>&1; then
  (cd examples/python && run_fix ruff-check-fix uv run ruff check --fix .) || true
  (cd examples/python && run_fix ruff-format uv run ruff format .) || true
fi

if should_run web && [ -f examples/web/package.json ] && command -v npm >/dev/null 2>&1; then
  if grep -q '"format"' examples/web/package.json 2>/dev/null; then
    (cd examples/web && run_fix web-format npm run format) || true
  fi
fi

if should_run node && [ -f examples/node/package.json ] && command -v npm >/dev/null 2>&1; then
  if grep -q '"format"' examples/node/package.json 2>/dev/null; then
    (cd examples/node && run_fix node-format npm run format) || true
  fi
fi

if should_run rust && [ -f examples/rust/Cargo.toml ] && command -v cargo >/dev/null 2>&1; then
  (cd examples/rust && run_fix rust-fmt cargo fmt) || true
fi

if should_run go && [ -f examples/go/go.mod ] && command -v gofmt >/dev/null 2>&1; then
  if [ -n "$PATHS" ]; then
    GO_FILES="$(find ${PATHS//,/ } -type f -name '*.go' 2>/dev/null | head -n 100 | tr '\n' ' ' || true)"
  else
    GO_FILES="$(git diff --name-only HEAD 2>/dev/null | grep '\.go$' | tr '\n' ' ' || true)"
    if [ -z "$GO_FILES" ] && [ -d examples/go ]; then
      GO_FILES="$(find examples/go -type f -name '*.go' 2>/dev/null | head -n 100 | tr '\n' ' ' || true)"
    fi
  fi
  if [ -n "$GO_FILES" ]; then
    # shellcheck disable=SC2086
    run_fix go-fmt gofmt -w $GO_FILES || true
  fi
fi

if command -v pre-commit >/dev/null 2>&1; then
  FILES=""
  if [ -n "$PATHS" ]; then
    FILES="$(find ${PATHS//,/ } -type f \( -name '*.md' -o -name '*.ts' -o -name '*.py' -o -name '*.js' -o -name '*.go' \) 2>/dev/null | head -n 50 | tr '\n' ' ')"
  else
    FILES="$(git diff --name-only HEAD 2>/dev/null | head -n 30 | tr '\n' ' ' || true)"
  fi
  if [ -n "$FILES" ]; then
    # shellcheck disable=SC2086
    run_fix pre-commit-whitespace pre-commit run trailing-whitespace end-of-file-fixer --files $FILES || true
  fi
fi

if [ -f scripts/normalize-markdown-whitespace.py ]; then
  for f in $(git diff --name-only HEAD 2>/dev/null | grep '\.md$' || true); do
    [ -f "$f" ] && run_fix markdown-ws $PY scripts/normalize-markdown-whitespace.py "$f" || true
  done
fi

if [ "$FIX_FAILED" -ne 0 ]; then
  echo "feature-autofix failed (one or more fixers errored)"
  exit 1
fi

echo "feature-autofix complete"
exit 0
