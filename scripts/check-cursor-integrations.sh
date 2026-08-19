#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TIER="foss"
ARGS=(--root "$ROOT")
while [ $# -gt 0 ]; do
  case "$1" in
    --tier) TIER="${2:-foss}"; shift 2 ;;
    --tier=*) TIER="${1#*=}"; shift ;;
    *) shift ;;
  esac
done
ARGS+=(--tier "$TIER")
python3 "$ROOT/scripts/lib/check_cursor_integrations.py" "${ARGS[@]}"
