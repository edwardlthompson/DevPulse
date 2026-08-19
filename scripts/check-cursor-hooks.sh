#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ARGS=(--root "$ROOT")
while [ $# -gt 0 ]; do
  case "$1" in
    --smoke) ARGS+=(--smoke); shift ;;
    *) shift ;;
  esac
done
python3 "$ROOT/scripts/lib/check_cursor_hooks.py" "${ARGS[@]}"
