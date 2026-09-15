#!/usr/bin/env bash
# Fail when AGENT.md is missing on a child, or BUILD_PLAN lost the brief.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/check_agent_brief.py" "$ROOT"
