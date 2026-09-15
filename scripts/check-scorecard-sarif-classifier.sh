#!/usr/bin/env bash
# Fail when the Scorecard SARIF classifier drifts from SECURITY_TRIAGE.md.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/scorecard_sarif.py" --docs
