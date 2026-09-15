#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
source "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/gen_feature_spec.py" "$@"
