#!/usr/bin/env bash
# Download pinned Android cmdline-tools. Never accept SDK licenses.
# Usage: scripts/install-android-cmdline-tools.sh [--apply]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

for arg in "$@"; do
  case "$arg" in
    *license*)
      echo "FAIL: this script never accepts SDK licenses (HUMAN/ADB on the host)"
      exit 1
      ;;
  esac
done

ZIP_NAME="commandlinetools-linux-11076708_latest.zip"
ZIP_URL="https://dl.google.com/android/repository/${ZIP_NAME}"
DEST="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-${HOME}/Android/Sdk}}"
DEST="${DEST}/cmdline-tools/latest"

echo "Android cmdline-tools pin: ${ZIP_URL}"
echo "Install dest: ${DEST}"
echo "Licenses: never auto-accepted. HUMAN/ADB accept them on the host."

if [ "${1:-}" != "--apply" ]; then
  echo "Dry-run. Re-run with --apply to download (network)."
  exit 0
fi

mkdir -p "$(dirname "$DEST")"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
curl -fsSL "$ZIP_URL" -o "$tmp/tools.zip"
rm -rf "$DEST"
mkdir -p "$DEST"
unzip -q "$tmp/tools.zip" -d "$tmp"
if [ -d "$tmp/cmdline-tools" ]; then
  # Zip root is cmdline-tools/; flatten into latest/
  shopt -s dotglob
  mv "$tmp/cmdline-tools"/* "$DEST/"
else
  mv "$tmp"/* "$DEST/"
fi
echo "OK cmdline-tools at ${DEST} (licenses still unaccepted)"
