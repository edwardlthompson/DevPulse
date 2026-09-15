#!/usr/bin/env bash
# Sync parent template gaps into BUILD_PLAN.md managed block (plan-only).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/sync_template_gaps_build_plan.py" --root "$ROOT" "$@"
