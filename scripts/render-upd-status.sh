#!/usr/bin/env bash
# Write .cursor/upd-status.md (and a Canvas file when Cursor canvases/ exists).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/upd_canvas.py"
