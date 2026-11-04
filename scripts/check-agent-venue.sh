#!/usr/bin/env bash
# Validate BUILD_PLAN LOCAL/CLOUD venue markers, tags, scopes, and overlap.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/agent_venue.py"
