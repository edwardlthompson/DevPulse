#!/usr/bin/env bash
# Fail locally when the commit-msg hook is missing. Skip in CI.
# Usage: scripts/check-pre-commit-hooks.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ "${CI:-}" = "true" ] || [ "${GITHUB_ACTIONS:-}" = "true" ] \
  || [ "${BOOTSTRAP_UPGRADE_SIM:-}" = "1" ]; then
  echo "OK   pre-commit commit-msg hook skipped in CI"
  exit 0
fi

if [ ! -d .git ]; then
  echo "OK   not a git checkout"
  exit 0
fi

HOOKS_DIR=".git/hooks"
CUSTOM="$(git config --get core.hooksPath || true)"
if [ -n "$CUSTOM" ]; then
  case "$CUSTOM" in
    /*) HOOKS_DIR="$CUSTOM" ;;
    *) HOOKS_DIR="$ROOT/$CUSTOM" ;;
  esac
fi

if [ -f "$HOOKS_DIR/commit-msg" ] && [ -s "$HOOKS_DIR/commit-msg" ]; then
  echo "OK   commit-msg hook installed ($HOOKS_DIR/commit-msg)"
  exit 0
fi

echo "FAIL: commit-msg hook missing. Install with:"
echo "  pip install pre-commit && pre-commit install --hook-type commit-msg"
exit 1
