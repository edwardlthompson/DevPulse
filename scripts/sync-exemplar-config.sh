#!/usr/bin/env bash
# Copy .example config stubs to runtime paths when live files are absent.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

copy_if_missing() {
  local example="$1"
  local dest="$2"
  if [ -f "$example" ] && [ ! -f "$dest" ]; then
    cp "$example" "$dest"
    echo "Synced exemplar config: $dest"
  fi
}

copy_if_missing "$ROOT/examples/web/public/app-update.json.example" \
  "$ROOT/examples/web/public/app-update.json"
copy_if_missing "$ROOT/examples/web/public/donations.json.example" \
  "$ROOT/examples/web/public/donations.json"
copy_if_missing "$ROOT/examples/android/app/src/main/assets/app-update.json.example" \
  "$ROOT/examples/android/app/src/main/assets/app-update.json"
copy_if_missing "$ROOT/examples/android/app/src/main/assets/donations.json.example" \
  "$ROOT/examples/android/app/src/main/assets/donations.json"
