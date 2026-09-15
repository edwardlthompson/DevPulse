#!/usr/bin/env bash
# Fail if local/agent scripts auto-accept Android SDK licenses (CI workflows allowlisted).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/android_sdk_licenses.py"
