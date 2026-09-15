#!/usr/bin/env bash
# Sync open Dependabot / Release Please PRs into BUILD_PLAN.md managed block.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/sync_open_prs_build_plan.py" --root "$ROOT" "$@"
