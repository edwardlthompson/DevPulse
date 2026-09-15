#!/usr/bin/env bash
# Local AOSP emulator + connectedDebugAndroidTest. Skip (exit 0) if SDK/hypervisor missing.
# Usage: scripts/run-android-emulator-local.sh [--keep-emulator] [--if-device]
# Never git push. Never adb reboot. Kill only an emulator this script started.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
KEEP=false
IF_DEVICE=false
EMU_PID=""

while [ $# -gt 0 ]; do
  case "$1" in
    --keep-emulator) KEEP=true; shift ;;
    --if-device) IF_DEVICE=true; shift ;;
    -h|--help)
      echo "Usage: scripts/run-android-emulator-local.sh [--keep-emulator] [--if-device]"
      echo "  --keep-emulator  do not kill an emulator this script started"
      echo "  --if-device      skip unless adb already lists a device (used by /gates)"
      exit 0
      ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

skip() { echo "SKIP: $*"; exit 0; }

cleanup() {
  if [ "$KEEP" = true ] || [ -z "${EMU_PID}" ]; then
    return 0
  fi
  kill "${EMU_PID}" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

if [ "${ANDROID_EMULATOR_LOCAL:-}" = "0" ]; then
  skip "ANDROID_EMULATOR_LOCAL=0"
fi

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -n "$SDK" ] && [ ! -d "$SDK" ]; then
  skip "ANDROID_HOME/ANDROID_SDK_ROOT is not a directory: $SDK"
fi
if [ -z "$SDK" ]; then
  for candidate in \
    "${LOCALAPPDATA:-}/Android/Sdk" \
    "${HOME}/AppData/Local/Android/Sdk" \
    "${HOME}/Library/Android/sdk" \
    "${HOME}/Android/Sdk"
  do
    if [ -d "$candidate" ]; then
      SDK="$candidate"
      break
    fi
  done
fi
[ -n "$SDK" ] && [ -d "$SDK" ] || skip "no Android SDK (set ANDROID_HOME)"

ADB="${ADB:-}"
if [ -z "$ADB" ]; then
  if [ -x "$SDK/platform-tools/adb" ]; then
    ADB="$SDK/platform-tools/adb"
  elif [ -x "$SDK/platform-tools/adb.exe" ]; then
    ADB="$SDK/platform-tools/adb.exe"
  elif command -v adb >/dev/null 2>&1; then
    ADB="$(command -v adb)"
  else
    skip "adb not found"
  fi
fi

devices="$("$ADB" devices 2>/dev/null | awk 'NR>1 && $2=="device"{print $1}')" || devices=""
run_tests() {
  bash "$ROOT/scripts/sync-exemplar-config.sh"
  (cd "$ROOT/examples/android" && chmod +x gradlew && ./gradlew connectedDebugAndroidTest --no-daemon)
}

if [ -n "$devices" ]; then
  echo "OK adb device: $(echo "$devices" | head -1)"
  run_tests
  exit 0
fi

if [ "$IF_DEVICE" = true ]; then
  skip "no adb device (--if-device; /gates will not download images)"
fi

EMU=""
if [ -x "$SDK/emulator/emulator" ]; then
  EMU="$SDK/emulator/emulator"
elif [ -x "$SDK/emulator/emulator.exe" ]; then
  EMU="$SDK/emulator/emulator.exe"
else
  skip "emulator binary not found"
fi

case "$(uname -m 2>/dev/null || echo x86_64)" in
  arm64|aarch64) ABI="arm64-v8a" ;;
  *) ABI="x86_64" ;;
esac
IMAGE="system-images;android-34;default;${ABI}"
AVD_NAME="goldenpath-api34"

if [ "$(uname -s 2>/dev/null)" = "Linux" ] && [ ! -e /dev/kvm ]; then
  skip "no /dev/kvm hypervisor"
fi

AVDMGR="$SDK/cmdline-tools/latest/bin/avdmanager"
[ -x "$AVDMGR" ] || AVDMGR="$SDK/cmdline-tools/latest/bin/avdmanager.bat"
SDKMGR="$SDK/cmdline-tools/latest/bin/sdkmanager"
[ -x "$SDKMGR" ] || SDKMGR="$SDK/cmdline-tools/latest/bin/sdkmanager.bat"

if ! "$EMU" -list-avds 2>/dev/null | grep -qx "$AVD_NAME"; then
  if [ ! -x "$AVDMGR" ] || [ ! -x "$SDKMGR" ]; then
    skip "AVD $AVD_NAME missing and avdmanager/sdkmanager not found (accept licenses with sdkmanager --licenses)"
  fi
  echo "WARN: creating AVD $AVD_NAME (may take minutes)"
  install_ok=1
  if command -v timeout >/dev/null 2>&1; then
    timeout 600 "$SDKMGR" "$IMAGE" >/dev/null 2>&1 || install_ok=0
  else
    "$SDKMGR" "$IMAGE" >/dev/null 2>&1 || install_ok=0
  fi
  if [ "$install_ok" -ne 1 ]; then
    skip "sdkmanager image install failed or timed out (accept licenses; rerun when network allows)"
  fi
  echo no | "$AVDMGR" create avd -n "$AVD_NAME" -k "$IMAGE" --force >/dev/null 2>&1 \
    || skip "avdmanager create failed (licenses?)"
fi

gpu="host"
"$EMU" -avd "$AVD_NAME" -gpu "$gpu" -no-snapshot -no-audio >/dev/null 2>&1 &
EMU_PID=$!
sleep 2
if ! kill -0 "$EMU_PID" 2>/dev/null; then
  echo "WARN: -gpu host failed; retry swiftshader_indirect"
  gpu="swiftshader_indirect"
  "$EMU" -avd "$AVD_NAME" -gpu "$gpu" -no-snapshot -no-audio >/dev/null 2>&1 &
  EMU_PID=$!
fi

echo "emulator gpu=$gpu pid=$EMU_PID"
"$ADB" wait-for-device
booted=0
for _ in $(seq 1 180); do
  if [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
    booted=1
    break
  fi
  sleep 1
done
if [ "$booted" -ne 1 ]; then
  echo "FAIL: emulator boot timeout 180s" >&2
  exit 1
fi
run_tests
