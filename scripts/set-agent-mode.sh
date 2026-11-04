#!/usr/bin/env bash
# Set repo-wide Agent Mode (speed|balanced|lean). ADR-0010.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" "$ROOT/scripts/lib/agent_mode.py" "$@"
