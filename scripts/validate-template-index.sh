#!/usr/bin/env bash
# Validate all paths in TEMPLATE_INDEX.json exist (Python-only).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/validate_template_index.py" "$ROOT"
