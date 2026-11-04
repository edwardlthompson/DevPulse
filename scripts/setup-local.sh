#!/usr/bin/env bash
# Opt-in local Ollama agent setup (dry-run default; --apply creates custom model).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/setup_local.py" "$@"
