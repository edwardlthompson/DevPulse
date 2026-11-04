#!/usr/bin/env bash
# Hardware-aware local model recommendation (opt-in; never requires GPU).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/recommend_model.py" "$@"
