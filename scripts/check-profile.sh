#!/usr/bin/env bash
# Validate bootstrap.config.json profile (core|full).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${1:-$ROOT}"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" -c "import sys; from pathlib import Path; from init_profile import check_profile; c,m=check_profile(Path(sys.argv[1])); print(m); raise SystemExit(c)" "$TARGET"
