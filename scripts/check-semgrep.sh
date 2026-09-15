#!/usr/bin/env bash
# FOSS Semgrep config gate; scan when semgrep is on PATH.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/semgrep_foss.py"
