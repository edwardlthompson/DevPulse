#!/usr/bin/env bash
# Local reproducible APK hash check (mirrors CI android-release job).
# Usage: scripts/verify-reproducible-apk.sh [--strict]
# Env: SOURCE_DATE_EPOCH (default 1700000000)
# Exit 0 when hashes match; 1 on build failure; 2 on hash mismatch (1 if --strict).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID="$ROOT/examples/android"
STRICT=false
SOURCE_DATE_EPOCH="${SOURCE_DATE_EPOCH:-1700000000}"

while [ $# -gt 0 ]; do
  case "$1" in
    --strict) STRICT=true; shift ;;
    -h|--help)
      echo "Usage: scripts/verify-reproducible-apk.sh [--strict]"
      exit 0
      ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [ ! -f "$ANDROID/gradlew" ]; then
  echo "ERROR: missing $ANDROID/gradlew"
  exit 1
fi

if ! command -v java >/dev/null 2>&1 && [ -z "${JAVA_HOME:-}" ]; then
  echo "ERROR: JAVA_HOME or java on PATH required"
  exit 1
fi

export SOURCE_DATE_EPOCH
cd "$ANDROID"
chmod +x gradlew

echo "Building release APK (SOURCE_DATE_EPOCH=${SOURCE_DATE_EPOCH})..."
./gradlew assembleRelease --no-daemon

APK="$(find app/build/outputs/apk/release -name '*.apk' 2>/dev/null | head -1 || true)"
if [ -z "$APK" ]; then
  echo "FAIL: no release APK found"
  exit 1
fi

if command -v sha256sum >/dev/null 2>&1; then
  HASH1="$(sha256sum "$APK" | awk '{print $1}')"
elif command -v shasum >/dev/null 2>&1; then
  HASH1="$(shasum -a 256 "$APK" | awk '{print $1}')"
else
  echo "ERROR: sha256sum or shasum required"
  exit 1
fi

echo "Run 1 hash: $HASH1"
echo "Clean rebuild..."
./gradlew clean assembleRelease --no-daemon -q

APK2="$(find app/build/outputs/apk/release -name '*.apk' 2>/dev/null | head -1 || true)"
if [ -z "$APK2" ]; then
  echo "FAIL: no release APK after clean rebuild"
  exit 1
fi

if command -v sha256sum >/dev/null 2>&1; then
  HASH2="$(sha256sum "$APK2" | awk '{print $1}')"
else
  HASH2="$(shasum -a 256 "$APK2" | awk '{print $1}')"
fi

echo "Run 2 hash: $HASH2"

if [ "$HASH1" = "$HASH2" ]; then
  echo "OK   Reproducible APK hash: $HASH1"
  exit 0
fi

echo "WARN: APK hashes differ between builds (documented flake tolerance in CI)"
if [ "$STRICT" = true ]; then
  exit 1
fi
exit 2
