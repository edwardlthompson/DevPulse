#!/usr/bin/env bash
# Fail when ADR-0001 pre-selects a pattern or drops the Sprint 1 pick row.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/adr_architecture.py"
