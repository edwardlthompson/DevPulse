#!/usr/bin/env bash
# Prove every ✅ BUILD_PLAN row in a finished sprint (startup + load order).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/sprint_smoke.py" --root "$ROOT" "$@"
