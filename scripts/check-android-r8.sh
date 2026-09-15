#!/usr/bin/env bash
# Fail when Golden Path release R8 rules or minify flags regress.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" -m unittest tests.test_android_runtime_budget