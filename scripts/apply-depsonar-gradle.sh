#!/usr/bin/env bash
# Apply depsonar Gradle plugin pins through patch-only + Kotlin <2.3.30 cap.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/gradle_apply.py" "$@"
