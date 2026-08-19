#!/usr/bin/env bash
# Attempt a BUILD_PLAN HUMAN/ADB row via automation catalog.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OWNER=""
TASK=""
SPRINT=""
JSON=false
while [ $# -gt 0 ]; do
  case "$1" in
    --owner) OWNER="${2:-}"; shift 2 ;;
    --owner=*) OWNER="${1#*=}" ;;
    --task) TASK="${2:-}"; shift 2 ;;
    --task=*) TASK="${1#*=}" ;;
    --sprint) SPRINT="${2:-}"; shift 2 ;;
    --sprint=*) SPRINT="${1#*=}" ;;
    --json) JSON=true; shift ;;
    -h|--help)
      echo "Usage: attempt-build-plan-row.sh --owner HUMAN|ADB --task \"...\" [--sprint NAME] [--json]"
      exit 0
      ;;
    *) shift ;;
  esac
done
if [ -z "$OWNER" ] || [ -z "$TASK" ]; then
  echo "ERROR: --owner and --task required" >&2
  exit 1
fi
ARGS=(--root "$ROOT" --owner "$OWNER" --task "$TASK" --sprint "$SPRINT")
[ "$JSON" = true ] && ARGS+=(--json)
python3 "$ROOT/scripts/lib/human_task_automation.py" "${ARGS[@]}"
