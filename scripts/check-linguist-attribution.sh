#!/usr/bin/env bash
# Child Linguist attribution gate (template exempt).
# Usage: bash scripts/check-linguist-attribution.sh [repo-root]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${1:-$ROOT}"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" "$ROOT/scripts/lib/check_linguist_attribution.py" "$TARGET"
