#!/usr/bin/env bash
# Cloud → PC handoff digest (fetch + open-PR sync + next AGENT row).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/resume_handoff.py" --root "$ROOT" "$@"
