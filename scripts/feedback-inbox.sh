#!/usr/bin/env bash
# Fail-soft inbox for /audit and /ideas. Empty JSON on timeout or missing gh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
cd "$ROOT"
exec "$PY" "$ROOT/scripts/lib/feedback_inbox_cli.py" "$@"
