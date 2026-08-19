#!/usr/bin/env bash
# Verify Android system bar inset handling via adb nav-mode switch + instrumented tests.
# Usage: scripts/verify-android-insets.sh [--screencap]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ADB="${ADB:-adb}"
ANDROID="$ROOT/examples/android"
SCREENCAP=false
TEST_CLASS="dev.foss.goldenpath.NavBarInsetUiTest"

while [ $# -gt 0 ]; do
  case "$1" in
    --screencap) SCREENCAP=true; shift ;;
    -h|--help)
      echo "Usage: scripts/verify-android-insets.sh [--screencap]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
  esac
done

if ! command -v "$ADB" >/dev/null 2>&1; then
  if [ -x "${LOCALAPPDATA:-}/Android/Sdk/platform-tools/adb.exe" ]; then
    ADB="${LOCALAPPDATA}/Android/Sdk/platform-tools/adb.exe"
  else
    echo "ERROR: adb not found; set ADB= path to platform-tools/adb"
    exit 1
  fi
fi

DEVICES="$("$ADB" devices | awk 'NR>1 && $2=="device"{print $1}')"
if [ -z "$DEVICES" ]; then
  echo "ERROR: no authorized adb device"
  "$ADB" devices -l
  exit 1
fi
echo "OK   device: $(echo "$DEVICES" | head -1)"

if [ -z "${JAVA_HOME:-}" ] && [ -x "/c/Program Files/Android/Android Studio/jbr/bin/java" ]; then
  export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
fi

run_insets_test() {
  local mode="$1"
  local label="$2"
  echo "=== navigation_mode=$mode ($label) ==="
  "$ADB" shell settings put secure navigation_mode "$mode"
  (cd "$ANDROID" && chmod +x gradlew && ./gradlew connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class="$TEST_CLASS" --no-daemon)
  if [ "$SCREENCAP" = true ]; then
    local out="/tmp/goldenpath-insets-${label}.png"
    "$ADB" exec-out screencap -p > "$out"
    echo "OK   screencap: $out"
  fi
}

run_insets_test 0 three-button
run_insets_test 2 gesture

echo "Android inset verification passed"
