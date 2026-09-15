#!/usr/bin/env bash
# Fail when SOURCE_DATE_EPOCH release APK wiring drops from CI or the local verifier.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/reproducible_apk_gate.py"
