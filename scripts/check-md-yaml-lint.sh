#!/usr/bin/env bash
# markdownlint + yamllint (non-blocking unless MDLINT_HARD=1). Skip if missing locally.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/md_yaml_lint.py"
