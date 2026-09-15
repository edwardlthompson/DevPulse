#!/usr/bin/env bash
# Lightroom Classic stub: Lr* SDK fields, no generic Lua require().
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIR="$ROOT/examples/lightroom"
if [ ! -d "$DIR" ]; then
  echo "SKIP lightroom (examples/lightroom missing)"
  exit 0
fi
test -f "$DIR/Info.lua"
grep -q 'LrSdkVersion' "$DIR/Info.lua"
grep -q 'LrExportServiceProvider' "$DIR/Info.lua"
grep -q 'LrMetadataTagsetFactory' "$DIR/Info.lua"
grep -q 'processRenderedPhotos' "$DIR/ExportServiceProvider.lua"
grep -q 'com.example.fossplugin.tagset' "$DIR/MetadataTagset.lua"
while IFS= read -r -d '' f; do
  if grep -nE "require\s*\(\s*['\"]" "$f" | grep -vE '^[[:space:]]*--'; then
    echo "ERROR: Generic Lua require() forbidden in $f (use Lr* SDK only)"
    exit 1
  fi
done < <(find "$DIR" -name '*.lua' -print0)
echo "Lightroom SDK namespace check passed"
