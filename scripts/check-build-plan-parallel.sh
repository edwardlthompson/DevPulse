#!/usr/bin/env bash
# Validate BUILD_PLAN sprints have Parallel tables with enough AGENT rows.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_PLAN="$ROOT/BUILD_PLAN.md"
MIN_AGENTS=2
while [ $# -gt 0 ]; do
  case "$1" in
    --min-agents)
      MIN_AGENTS="${2:-2}"
      shift 2
      ;;
    --min-agents=*)
      MIN_AGENTS="${1#*=}"
      shift
      ;;
    --draft)
      BUILD_PLAN="${2:-$BUILD_PLAN}"
      shift 2
      ;;
    --draft=*)
      BUILD_PLAN="${1#*=}"
      shift
      ;;
    *)
      shift
      ;;
  esac
done
python3 "$ROOT/scripts/lib/parallel_scope_cli.py" \
  --build-plan "$BUILD_PLAN" \
  check-build-plan \
  --min-agents "$MIN_AGENTS"
