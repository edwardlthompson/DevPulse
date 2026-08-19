#!/usr/bin/env bash
# Conventional Commits subject check (commit-msg hook).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
source "$ROOT/scripts/lib/resolve-python.sh"

export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
if [ $# -gt 0 ]; then
  exec "$PY" "$ROOT/scripts/lib/check_conventional_commit.py" "$1"
fi
exec "$PY" "$ROOT/scripts/lib/check_conventional_commit.py"
