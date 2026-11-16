#!/usr/bin/env bash
# Validate F-Droid/Fastlane metadata (AGENT gate; APK hashes remain ADB).
# Usage: scripts/verify-fdroid-metadata.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FASTLANE="$ROOT/fastlane/metadata/android/en-US"
GRADLE="$ROOT/examples/android/app/build.gradle.kts"
ERRORS=0

fail() {
  echo "FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

ok() {
  echo "OK   $1"
}

if [ ! -d "$FASTLANE" ]; then
  fail "missing $FASTLANE"
else
  ok "root fastlane metadata present"
fi

for f in title.txt short_description.txt full_description.txt; do
  if [ ! -s "$FASTLANE/$f" ]; then
    fail "missing or empty $FASTLANE/$f"
  else
    ok "$f present"
  fi
done

AF="$FASTLANE/antifeatures.yml"
if [ ! -s "$AF" ]; then
  fail "missing $AF"
elif ! grep -q "NonFreeNet" "$AF"; then
  fail "antifeatures.yml must declare NonFreeNet"
elif ! grep -q "Google Play" "$AF" || ! grep -q "GitHub" "$AF" || ! grep -q "Aptoide" "$AF"; then
  fail "NonFreeNet description must mention Google Play, GitHub, and Aptoide"
else
  ok "NonFreeNet describes Play, GitHub, and Aptoide lookups"
fi

VERSION_CODE="$(python3 -c 'import json; print(json.load(open("schemas/golden-path/app-version.json"))["versionCode"])')"
if [ -z "${VERSION_CODE:-}" ]; then
  fail "could not parse versionCode from app-version.json"
elif [ ! -s "$FASTLANE/changelogs/${VERSION_CODE}.txt" ]; then
  fail "missing changelog $FASTLANE/changelogs/${VERSION_CODE}.txt"
else
  ok "changelog for versionCode ${VERSION_CODE}"
fi

if [ ! -s "$FASTLANE/images/icon.png" ]; then
  fail "missing $FASTLANE/images/icon.png"
else
  ok "fastlane icon.png present"
fi

if [ ! -d "$FASTLANE/images/phoneScreenshots" ]; then
  fail "missing $FASTLANE/images/phoneScreenshots/"
else
  ok "phoneScreenshots directory present"
fi

if [ -f "$ROOT/LICENSE" ] && grep -q "GNU GENERAL PUBLIC LICENSE" "$ROOT/LICENSE"; then
  FIRST="$(head -n 1 "$ROOT/LICENSE" | sed 's/^[[:space:]]*//')"
  if [ "$FIRST" != "GNU GENERAL PUBLIC LICENSE" ]; then
    fail "LICENSE must start with canonical GNU GPL v3 text (not a project header)"
  elif ! grep -q "<one line to give the program" "$ROOT/LICENSE"; then
    fail "LICENSE How to Apply section is truncated; GitHub licensee needs the canonical GPL-3.0 text"
  else
    ok "root LICENSE is canonical GPL-3.0"
  fi
else
  fail "missing canonical GPL-3.0 LICENSE"
fi

if [ ! -f "$GRADLE" ]; then
  fail "missing $GRADLE"
elif grep -q 'getByName("release")' "$GRADLE"; then
  fail 'build.gradle.kts must not use signingConfigs.getByName("release")'
elif ! grep -q "maybeCreate" "$GRADLE" || ! grep -q "findByName" "$GRADLE"; then
  fail "build.gradle.kts must use signingConfigs.maybeCreate/findByName"
else
  ok "release signingConfig is strip-tolerant"
fi

NESTED="$ROOT/examples/android/fastlane/metadata/android/en-US"
if [ -d "$NESTED" ]; then
  for f in title.txt short_description.txt full_description.txt; do
    if [ ! -s "$NESTED/$f" ]; then
      fail "missing or empty $NESTED/$f"
    else
      ok "nested fastlane $f present"
    fi
  done
fi

echo ""
echo "SKIP [ADB] reproducible APK hash verification — run on device/emulator per modules/android/MODULE.md"

if ! bash "$ROOT/scripts/check-fdroid-screenshots.sh"; then
  fail "dummy F-Droid screenshots are not allowed"
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} F-Droid metadata check(s) failed"
  exit 1
fi

echo "F-Droid metadata scaffold verified"
