#!/usr/bin/env bash
# Best-effort local commit-msg hook install (init / setup).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"

if [ -f .git/hooks/commit-msg ] && [ -s .git/hooks/commit-msg ]; then
  echo "OK   commit-msg hook already present"
  exit 0
fi

install_ok=1
if command -v pre-commit >/dev/null 2>&1; then
  pre-commit install --hook-type commit-msg || install_ok=0
elif "$PY" -m pre_commit --help >/dev/null 2>&1; then
  "$PY" -m pre_commit install --hook-type commit-msg || install_ok=0
else
  echo "WARN: pre-commit not installed. Run:"
  echo "  pip install pre-commit && pre-commit install --hook-type commit-msg"
  exit 0
fi

if [ "$install_ok" -eq 1 ] && [ -s .git/hooks/commit-msg ]; then
  echo "OK   commit-msg hook installed"
  exit 0
fi
echo "WARN: could not install commit-msg hook"
exit 0
