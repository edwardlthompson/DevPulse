#!/usr/bin/env bash
# Weekly Cursor feature radar — soft-fail (exit 0 on network errors)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
python3 "$ROOT/scripts/lib/cursor_feature_radar.py" --root "$ROOT"
