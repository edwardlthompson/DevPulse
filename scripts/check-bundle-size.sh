#!/usr/bin/env bash
# Fail if Vite dist JS exceeds gzip budget (default 200 KB total).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WEB="$ROOT/examples/web"
MAX_KB="${BUNDLE_MAX_KB:-200}"
MAX_BYTES=$((MAX_KB * 1024))
DIST="$WEB/dist"

if [ ! -d "$DIST" ]; then
  echo "ERROR: $DIST not found; run npm run build in examples/web first"
  exit 1
fi

total=0
shopt -s nullglob
for f in "$DIST"/assets/*.js "$DIST"/*.js; do
  [ -f "$f" ] || continue
  gz=$(gzip -c "$f" | wc -c | tr -d ' ')
  total=$((total + gz))
  echo "  $(basename "$f"): ${gz} bytes gzip"
done

echo "Total dist JS (gzip): ${total} bytes (budget: ${MAX_BYTES})"
if [ "$total" -gt "$MAX_BYTES" ]; then
  echo "ERROR: Bundle exceeds ${MAX_KB} KB gzip budget"
  exit 1
fi
echo "Bundle size check passed"
