#!/usr/bin/env bash
# Merge Unreleased + HUMAN/ADB rows into .cursor-session-state.json (/compact).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/session_compact.py"
