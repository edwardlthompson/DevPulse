#!/usr/bin/env bash
# Require docs/MERGE_QUEUE.md (optional queue; required checks stay listed).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/merge_queue_docs.py"
