#!/usr/bin/env bash
# Sanitized issue composer for agents. Usage: python3 scripts/agent-run.py report-issue -- --print --type bug
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/report_issue.py" "$@"
