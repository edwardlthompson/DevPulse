#!/usr/bin/env bash
# Schema-check packaging/winget/manifest.stub.yaml (does not submit).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FILE="${1:-$ROOT/packaging/winget/manifest.stub.yaml}"
if [ ! -f "$FILE" ]; then
  # Fall back to committed example when stub is not generated yet.
  if [ -f "$ROOT/packaging/winget/example/manifest.yaml" ]; then
    FILE="$ROOT/packaging/winget/example/manifest.yaml"
  else
    echo "SKIP Winget stub (generated in release.yml before this check)"
    exit 0
  fi
fi
ERRORS=0
need() {
  if ! grep -qE "^$1:" "$FILE"; then
    echo "FAIL: missing $1 in $FILE"
    ERRORS=$((ERRORS + 1))
  fi
}
need PackageIdentifier
need PackageVersion
need ManifestVersion
need License
if ! grep -q 'InstallerSha256:' "$FILE"; then
  echo "FAIL: missing InstallerSha256"
  ERRORS=$((ERRORS + 1))
fi
if ! grep -q 'Architecture: arm64' "$FILE"; then
  echo "FAIL: missing Architecture: arm64 installer row"
  ERRORS=$((ERRORS + 1))
fi
# Each InstallerUrl under arm64 (and overall) must be https://
while IFS= read -r url; do
  case "$url" in
    https://*) ;;
    *)
      echo "FAIL: InstallerUrl must be https:// — got: $url"
      ERRORS=$((ERRORS + 1))
      ;;
  esac
done < <(grep -E '^\s+InstallerUrl:' "$FILE" | sed -E 's/.*InstallerUrl:[[:space:]]*//')
# Explicit arm64 URL presence
if ! awk '
  /Architecture: arm64/ { in_arm=1; next }
  /Architecture:/ { in_arm=0 }
  in_arm && /InstallerUrl:/ {
    if ($0 ~ /https:\/\//) found=1
  }
  END { exit found ? 0 : 1 }
' "$FILE"; then
  echo "FAIL: arm64 installer row missing https InstallerUrl"
  ERRORS=$((ERRORS + 1))
fi
if [ "$ERRORS" -gt 0 ]; then
  exit 1
fi
echo "OK   Winget stub schema + arm64 https URL ($FILE)"
