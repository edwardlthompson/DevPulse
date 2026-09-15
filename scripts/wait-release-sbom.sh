#!/usr/bin/env bash
# Poll GitHub Release assets until sbom.cyclonedx.json and openvex.json appear (or timeout).
# Usage: scripts/wait-release-sbom.sh [tag] [--wait SEC] [--once] [--require]
#   --once     check once via `gh release view` (no poll); exit 1 if assets missing
#   --require  fail (exit 1) when gh is missing or no tag (post-tag smoke / /regress)
set -euo pipefail
WAIT=300
TAG=""
ONCE=0
REQUIRE=0
while [ $# -gt 0 ]; do
  case "$1" in
    --wait) WAIT="${2:-300}"; shift 2 ;;
    --wait=*) WAIT="${1#*=}"; shift ;;
    --once) ONCE=1; WAIT=0; shift ;;
    --require) REQUIRE=1; shift ;;
    *) TAG="$1"; shift ;;
  esac
done

fail_or_warn() {
  local msg="$1"
  if [ "$REQUIRE" -eq 1 ]; then
    echo "FAIL: $msg"
    exit 1
  fi
  echo "WARN: $msg"
  exit 0
}

if ! command -v gh >/dev/null 2>&1; then
  fail_or_warn "gh not installed; skip SBOM wait"
fi

if [ -z "$TAG" ]; then
  TAG="$(gh release view --json tagName -q .tagName 2>/dev/null || true)"
fi
if [ -z "$TAG" ]; then
  fail_or_warn "no release tag; skip SBOM wait"
fi

assets_ok() {
  local names
  names="$(gh release view "$TAG" --json assets -q '.assets[].name' 2>/dev/null || true)"
  echo "$names" | grep -qx 'sbom.cyclonedx.json' && echo "$names" | grep -qx 'openvex.json'
}

if [ "$ONCE" -eq 1 ] || [ "$WAIT" -eq 0 ]; then
  if assets_ok; then
    echo "OK   SBOM + OpenVEX assets on $TAG (gh release view)"
    exit 0
  fi
  echo "FAIL: $TAG missing sbom.cyclonedx.json and/or openvex.json (gh release view)"
  exit 1
fi

deadline=$((SECONDS + WAIT))
while [ "$SECONDS" -lt "$deadline" ]; do
  if assets_ok; then
    echo "OK   SBOM + OpenVEX assets on $TAG"
    exit 0
  fi
  sleep 15
done
echo "FAIL: timed out after ${WAIT}s waiting for sbom.cyclonedx.json and openvex.json on $TAG"
exit 1
