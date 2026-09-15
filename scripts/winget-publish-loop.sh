#!/usr/bin/env bash
# Hash desktop installer files (or a local dry-run zip) and write a Winget stub.
# Does not submit to microsoft/winget-pkgs. [HUMAN] opens that PR.
# Usage:
#   scripts/winget-publish-loop.sh --dry-run
#   scripts/winget-publish-loop.sh --dry-run --from-release
#   WINGET_INSTALLER_X64=dist/app-x64.zip WINGET_INSTALLER_ARM64=dist/app-arm64.zip \
#     scripts/winget-publish-loop.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

OUT_DIR="${WINGET_LOOP_OUT:-$ROOT/dist/winget-loop}"
STUB="$OUT_DIR/manifest.stub.yaml"
DRY=0
FROM_RELEASE=0
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY=1 ;;
    --from-release) FROM_RELEASE=1 ;;
  esac
done

mkdir -p "$OUT_DIR"
X64="${WINGET_INSTALLER_X64:-}"
ARM64="${WINGET_INSTALLER_ARM64:-}"
URL_BASE=""

if [ "$FROM_RELEASE" = "1" ] && command -v gh >/dev/null 2>&1; then
  REL_DIR="$OUT_DIR/release-assets"
  rm -rf "$REL_DIR"
  mkdir -p "$REL_DIR"
  TAG="$(gh release view --json tagName -q .tagName 2>/dev/null || true)"
  if [ -n "$TAG" ]; then
    # Download any installer-like assets; ignore failure when Release has none.
    gh release download "$TAG" -D "$REL_DIR" -p "*.zip" -p "*.msi" -p "*.exe" 2>/dev/null || true
    mapfile -t ZIPS < <(find "$REL_DIR" -type f \( -name "*.zip" -o -name "*.msi" -o -name "*.exe" \) | sort)
    if [ "${#ZIPS[@]}" -gt 0 ]; then
      X64="${ZIPS[0]}"
      if [ "${#ZIPS[@]}" -gt 1 ]; then
        ARM64="${ZIPS[1]}"
      fi
      REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
      if [ -n "$REPO" ]; then
        URL_BASE="https://github.com/${REPO}/releases/download/${TAG}"
      fi
      echo "NOTE using Release $TAG asset(s) for Winget dry-run hash"
    else
      echo "NOTE Release $TAG has no zip/msi/exe assets — falling back to packed dry-run zip"
    fi
  else
    echo "NOTE no GitHub Release found — falling back to packed dry-run zip"
  fi
elif [ "$FROM_RELEASE" = "1" ]; then
  echo "NOTE gh CLI missing — cannot pull Release assets; packed dry-run zip"
fi

if [ -z "$X64" ] && [ "$DRY" = "1" ]; then
  X64="$OUT_DIR/goldenpath-dry-x64.zip"
  printf "Golden Path Winget dry-run\n" > "$OUT_DIR/README.txt"
  if command -v zip >/dev/null 2>&1; then
    (cd "$OUT_DIR" && zip -q -X "goldenpath-dry-x64.zip" README.txt)
  else
    tar -C "$OUT_DIR" -cf "$X64" README.txt
  fi
  echo "NOTE dry-run packed $X64 — replace with Windows Release assets before submit"
fi

if [ -z "$X64" ] || [ ! -f "$X64" ]; then
  echo "FAIL: set WINGET_INSTALLER_X64 to an existing installer, or pass --dry-run"
  exit 1
fi

ARGS=(--x64 "$X64" --out "$STUB")
if [ -n "$ARM64" ] && [ -f "$ARM64" ]; then
  ARGS+=(--arm64 "$ARM64")
else
  echo "NOTE arm64 installer omitted — set WINGET_INSTALLER_ARM64 for both arches"
fi
if [ -n "$URL_BASE" ]; then
  ARGS+=(--url-base "$URL_BASE")
fi
if [ -n "${TAG:-}" ]; then
  ARGS+=(--version "${TAG#v}")
fi

"$PY" "$ROOT/scripts/lib/winget_publish_loop.py" "${ARGS[@]}"
bash "$ROOT/scripts/validate-winget-stub.sh" "$STUB"
echo "OK   Winget loop wrote $STUB (no submit)"
