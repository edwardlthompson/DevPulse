#!/usr/bin/env bash
# Remove stale .cursor/parallel-scope-lock.json (empty, invalid, or older than 24h).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/parallel_lock_gc.py"
