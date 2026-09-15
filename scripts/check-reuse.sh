#!/usr/bin/env bash
# REUSE/SPDX lint. Skip if reuse CLI is missing locally; required in CI.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/reuse_check.py"
