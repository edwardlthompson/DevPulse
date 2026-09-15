#!/usr/bin/env bash
# PSScriptAnalyzer -Severity Error on scripts/*.ps1 + worktree setup. Skip if missing locally.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/psscriptanalyzer_check.py"
