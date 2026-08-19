#!/usr/bin/env bash
# Pre-release gate: CI green, zero Critical/High Dependabot alerts, template version present.
# Usage: scripts/pre-release-gate.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ERRORS=0
VERSION=""

echo "=== Pre-release gate ==="

if ! bash scripts/feature-gate.sh --stack multi --strict --json; then
  echo "FAIL: feature-gate.sh"
  ERRORS=$((ERRORS + 1))
else
  echo "OK   feature-gate.sh passed"
fi

if ! bash scripts/check-security-triage.sh --wait-ci 300 --strict; then
  echo "FAIL: security-triage.sh --strict"
  ERRORS=$((ERRORS + 1))
else
  echo "OK   security-triage.sh --strict passed"
fi

if [ ! -f .template-version ]; then
  echo "MISSING: .template-version"
  ERRORS=$((ERRORS + 1))
else
  VERSION="$(tr -d '[:space:]' < .template-version)"
  echo "OK   .template-version = ${VERSION}"
  if [ -f .release-please-manifest.json ]; then
    MANIFEST_VERSION="$(python3 - <<'PY'
import json
with open(".release-please-manifest.json", encoding="utf-8") as f:
    print(json.load(f).get(".", "").strip())
PY
)"
    if [ -z "$MANIFEST_VERSION" ]; then
      echo "FAIL: .release-please-manifest.json missing \".\" version"
      ERRORS=$((ERRORS + 1))
    elif [ "$VERSION" != "$MANIFEST_VERSION" ]; then
      echo "FAIL: .template-version (${VERSION}) != release-please manifest (${MANIFEST_VERSION})"
      ERRORS=$((ERRORS + 1))
    else
      echo "OK   release-please manifest matches .template-version"
    fi
  else
    echo "FAIL: .release-please-manifest.json not found"
    ERRORS=$((ERRORS + 1))
  fi
fi

if ! bash scripts/check-license-compliance.sh; then
  echo "FAIL: check-license-compliance.sh"
  ERRORS=$((ERRORS + 1))
else
  echo "OK   check-license-compliance.sh passed"
fi

if command -v gh >/dev/null 2>&1; then
  if ! bash scripts/verify-branch-protection.sh; then
    echo "FAIL: verify-branch-protection.sh (Windows upgrade-sim must be required)"
    ERRORS=$((ERRORS + 1))
  else
    echo "OK   verify-branch-protection.sh passed"
  fi
else
  echo "SKIP verify-branch-protection.sh (gh not on PATH)"
fi

echo ""
echo "REMINDER: Before tagging, trigger the Release workflow via workflow_dispatch:"
echo "  GitHub -> Actions -> Release -> Run workflow"
echo "  (.github/workflows/release.yml)"
if [ -n "$VERSION" ]; then
  echo "  Confirm CHANGELOG.md [${VERSION}] section and tag match .template-version"
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} pre-release gate check(s) failed"
  exit 1
fi

echo "Pre-release gate passed"
