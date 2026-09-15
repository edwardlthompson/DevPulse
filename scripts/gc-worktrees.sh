#!/usr/bin/env bash
# Prune git worktree metadata and GC stale .cursor/worktrees/ (parallel-lock isolation).
# Default: dry-run. Pass --apply to delete stale/orphan dirs older than 24h.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/worktree_gc.py" "$@"
