#!/usr/bin/env bash
# Render icon-factory jobs (Blender runtime when present).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
CLI="$ROOT/examples/blender/cli.py"
if command -v blender >/dev/null 2>&1 && [ "${BLENDER_ICONS_STUB:-}" != "1" ]; then
  exec blender --background --python "$CLI" -- "$@"
fi
exec "$PY" "$CLI" --stub "$@"
