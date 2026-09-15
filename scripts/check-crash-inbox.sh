#!/usr/bin/env bash
# Fail when the GlitchTip/Bugsink stub is enabled or ships a live DSN.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/crash_inbox.py"
