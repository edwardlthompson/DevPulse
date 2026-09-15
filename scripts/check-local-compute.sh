#!/usr/bin/env bash
# Local compute INFO probe (CPU/RAM/Ollama/emulator). Never a CI hard-fail except misconfig.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/check_local_compute.py" "$@"
