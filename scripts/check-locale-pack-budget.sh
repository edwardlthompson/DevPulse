#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/locale_pack_budget.py"
