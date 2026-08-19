#!/usr/bin/env bash
# Build parallel agent dispatch manifest from BUILD_PLAN Parallel tables.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_PLAN="$ROOT/BUILD_PLAN.md"
JSON=false
SUGGEST=false
REQUIRE_SEQ=false
STACK=""
FEATURE=""
DRAFT=""
while [ $# -gt 0 ]; do
  case "$1" in
    --json) JSON=true ;;
    --suggest) SUGGEST=true ;;
    --require-sequential-clear) REQUIRE_SEQ=true ;;
    --stack) shift; STACK="${1:-}" ;;
    --stack=*) STACK="${1#*=}" ;;
    --feature) shift; FEATURE="${1:-}" ;;
    --feature=*) FEATURE="${1#*=}" ;;
    --draft) shift; DRAFT="${1:-}" ;;
    --draft=*) DRAFT="${1#*=}" ;;
    -h|--help)
      echo "Usage: plan-parallel-dispatch.sh [--json] [--suggest] [--require-sequential-clear]"
      echo "       [--stack NAME] [--feature NAME] [--draft PATH]"
      exit 0
      ;;
  esac
  shift
done
if [ -n "$DRAFT" ]; then
  BUILD_PLAN="$DRAFT"
fi
ARGS=(--build-plan "$BUILD_PLAN" manifest)
[ "$JSON" = true ] && ARGS+=(--json)
[ "$SUGGEST" = true ] && ARGS+=(--suggest)
[ "$REQUIRE_SEQ" = true ] && ARGS+=(--require-sequential-clear)
[ -n "$STACK" ] && ARGS+=(--stack "$STACK")
[ -n "$FEATURE" ] && ARGS+=(--feature "$FEATURE")
python3 "$ROOT/scripts/lib/parallel_scope_cli.py" "${ARGS[@]}"
