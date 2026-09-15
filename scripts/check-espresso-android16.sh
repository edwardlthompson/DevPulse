#!/usr/bin/env bash
# Fail if Golden Path Android pins Espresso below 3.7.0 (Android 16 removed InputManager.getInstance).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GRADLE="$ROOT/examples/android/app/build.gradle.kts"
if [ ! -f "$GRADLE" ]; then
  echo "SKIP: examples/android pruned"
  exit 0
fi
if ! grep -qE 'espresso-core:3\.(7|[89]|[1-9][0-9])\.' "$GRADLE"; then
  echo "FAIL: pin androidx.test.espresso:espresso-core to 3.7.0+ for Android 16 (see InputManager.getInstance)."
  echo "  file: $GRADLE"
  exit 1
fi
echo "OK: espresso-core >= 3.7.0 pinned"
