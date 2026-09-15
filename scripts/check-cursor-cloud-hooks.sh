#!/usr/bin/env bash
# Fail when merging commercial Cloud hooks would drop FOSS shell/encoding guards.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/cursor_cloud_hooks.py"
