#!/usr/bin/env bash
# F-Droid device dry-run: metadata gate + release APK install + smoke launch + logcat scan.
# Usage: scripts/fdroid-device-dry-run.sh
# Requires: adb device authorized, JAVA_HOME or Android Studio JBR on PATH.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ADB="${ADB:-adb}"
PKG="dev.foss.goldenpath"
ANDROID="$ROOT/examples/android"
APK_DIR="$ANDROID/app/build/outputs/apk/debug"
BUILD_TASK="assembleDebug"
LOG="/tmp/fdroid-dry-run-logcat-$$.txt"

if ! command -v "$ADB" >/dev/null 2>&1; then
  if [ -x "${LOCALAPPDATA:-}/Android/Sdk/platform-tools/adb.exe" ]; then
    ADB="${LOCALAPPDATA}/Android/Sdk/platform-tools/adb.exe"
  else
    echo "ERROR: adb not found; set ADB= path to platform-tools/adb"
    exit 1
  fi
fi

echo "=== F-Droid metadata ==="
bash scripts/verify-fdroid-metadata.sh

DEVICES="$("$ADB" devices | awk 'NR>1 && $2=="device"{print $1}')"
if [ -z "$DEVICES" ]; then
  echo "ERROR: no authorized adb device (check USB debugging + RSA prompt)"
  "$ADB" devices -l
  exit 1
fi
echo "OK   device: $(echo "$DEVICES" | head -1)"

if [ -z "${JAVA_HOME:-}" ] && [ -x "/c/Program Files/Android/Android Studio/jbr/bin/java" ]; then
  export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
fi

APK="$(find "$APK_DIR" -name '*.apk' 2>/dev/null | head -1 || true)"
if [ -z "$APK" ]; then
  echo "Building release APK..."
  export SOURCE_DATE_EPOCH="${SOURCE_DATE_EPOCH:-1700000000}"
  (cd "$ANDROID" && chmod +x gradlew && ./gradlew "$BUILD_TASK" --no-daemon)
  APK="$(find "$APK_DIR" -name '*.apk' | head -1)"
fi
echo "OK   APK: $APK"

"$ADB" logcat -c || true
echo "Installing..."
"$ADB" install -r "$APK"

echo "Launching $PKG..."
"$ADB" shell am start -n "$PKG/.MainActivity" || "$ADB" shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1

sleep 5
"$ADB" logcat -d > "$LOG" || true

if grep -E 'FATAL EXCEPTION|AndroidRuntime.*E' "$LOG" >/dev/null 2>&1; then
  echo "FAIL: crash signatures in logcat"
  grep -E 'FATAL EXCEPTION|AndroidRuntime' "$LOG" | tail -20
  exit 1
fi

echo "OK   no FATAL EXCEPTION in logcat (saved: $LOG)"
echo "F-Droid device dry-run passed"
