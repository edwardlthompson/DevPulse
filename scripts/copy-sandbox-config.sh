#!/usr/bin/env bash
# Copy sandbox.json.example → gitignored .cursor/sandbox.json when missing.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/.cursor/sandbox.json.example"
DEST="$ROOT/.cursor/sandbox.json"
if [ ! -f "$SRC" ]; then
  echo "SKIP sandbox copy (no .cursor/sandbox.json.example)"
  exit 0
fi
if [ -f "$DEST" ]; then
  echo "OK   .cursor/sandbox.json already present"
  exit 0
fi
cp "$SRC" "$DEST"
echo "OK   copied .cursor/sandbox.json.example → .cursor/sandbox.json"
