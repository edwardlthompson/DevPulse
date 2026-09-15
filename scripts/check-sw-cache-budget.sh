#!/usr/bin/env bash
# Fail when examples/web service-worker PRECACHE exceeds the offline shell budget.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/sw_cache_budget.py"
