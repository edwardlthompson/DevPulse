#!/usr/bin/env bash
# Run verify.sh --quick and print a first-failure interpretation for /tour.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
LOG="$(mktemp)"
set +e
"$PY" "$ROOT/scripts/agent-run.py" verify -- --quick >"$LOG" 2>&1
CODE=$?
set -e
"$PY" "$ROOT/scripts/lib/verify_first_failure.py" --exit "$CODE" --log "$LOG"
rm -f "$LOG"
exit "$CODE"
