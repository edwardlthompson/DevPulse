#!/usr/bin/env bash
# Detect overlapping isolated scopes in BUILD_PLAN Parallel tables.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_PLAN="${1:-$ROOT/BUILD_PLAN.md}"
python3 "$ROOT/scripts/lib/parallel_scope_cli.py" --build-plan "$BUILD_PLAN" check-overlap
