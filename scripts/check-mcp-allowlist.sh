#!/usr/bin/env bash
# Fail when beforeMCPExecution is missing the FOSS MCP server allowlist.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/mcp_allowlist.py"
