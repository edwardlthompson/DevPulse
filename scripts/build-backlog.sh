#!/usr/bin/env bash
# Append failed HUMAN/ADB automation attempts to HUMAN_BACKLOG.md.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CMD="${1:-}"
shift || true
python3 "$ROOT/scripts/lib/build_backlog.py" --root "$ROOT" "$CMD" "$@"
