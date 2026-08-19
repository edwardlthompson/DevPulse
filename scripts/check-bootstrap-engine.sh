#!/usr/bin/env bash
# Unit tests for bootstrap lifecycle engine (stdlib unittest).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
source "$ROOT/scripts/lib/resolve-python.sh"

export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" -m unittest discover -s tests -p "test_*.py" -q
