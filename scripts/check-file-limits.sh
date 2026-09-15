#!/usr/bin/env bash
# Enforce file line limits: 300 for static data (UI + i18n), 150 for pure logic.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=/dev/null
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/check_file_limits.py"
