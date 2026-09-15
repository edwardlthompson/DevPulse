#!/usr/bin/env bash
# Fail when the Android signing/rollback runbook or env-only Gradle signing drops.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/android_signing_runbook.py"
