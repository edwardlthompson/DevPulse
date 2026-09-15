#!/usr/bin/env bash
# Detect overlapping isolated scopes in BUILD_PLAN Parallel tables.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_PLAN="${1:-$ROOT/BUILD_PLAN.md}"
if [ "${1:-}" = "--dry-run" ] || [ "${2:-}" = "--dry-run" ]; then
  if [ "${1:-}" = "--dry-run" ]; then
    BUILD_PLAN="${2:-$ROOT/BUILD_PLAN.md}"
  fi
  python3 "$ROOT/scripts/lib/parallel_scope_cli.py" --build-plan "$BUILD_PLAN" dry-run
  exit $?
fi
python3 "$ROOT/scripts/lib/parallel_scope_cli.py" --build-plan "$BUILD_PLAN" check-overlap
