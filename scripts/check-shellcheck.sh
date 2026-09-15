#!/usr/bin/env bash
# Run shellcheck at -S error on scripts/*.sh. Skip if missing locally; required in CI.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/shellcheck_scripts.py"
