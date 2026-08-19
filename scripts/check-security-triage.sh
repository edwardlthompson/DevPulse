#!/usr/bin/env bash
# Automated weekly security triage checks (Dependabot + required workflows + Scorecard).
# Usage: scripts/check-security-triage.sh [--wait-ci SEC] [--strict]
#   --strict  Fail on Dependabot API errors and missing/failed Scorecard (pre-release).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

WAIT_CI=0
STRICT=false
while [ $# -gt 0 ]; do
  case "$1" in
    --wait-ci) WAIT_CI="${2:-300}"; shift 2 ;;
    --strict) STRICT=true; shift ;;
    *) shift ;;
  esac
done

ERRORS=0

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required"
  exit 1
fi

REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
if [ -z "$REPO" ]; then
  echo "ERROR: gh auth required"
  exit 1
fi

echo "=== Security triage (automated) for ${REPO} ==="

ALERT_COUNT="$(bash scripts/count-critical-high-dependabot.sh 2>/dev/null || echo error)"

if [ "$ALERT_COUNT" = "error" ]; then
  echo "WARN: could not fetch Dependabot alerts (gh auth may need security_events scope)"
  if [ "$STRICT" = true ]; then
    ERRORS=$((ERRORS + 1))
  fi
elif [ "${ALERT_COUNT:-0}" -gt 0 ]; then
  echo "FAIL: ${ALERT_COUNT} open Critical/High Dependabot alert(s)"
  ERRORS=$((ERRORS + 1))
else
  echo "OK   Zero open Critical/High Dependabot alerts"
fi

if [ "$WAIT_CI" -gt 0 ]; then
  if bash scripts/check-github-ci.sh HEAD --wait "$WAIT_CI"; then
    echo "OK   CI, Security Scan, CodeQL green on HEAD"
  else
    echo "FAIL: required workflows not green"
    ERRORS=$((ERRORS + 1))
  fi
else
  if bash scripts/check-github-ci.sh HEAD 2>/dev/null; then
    echo "OK   Required workflows green on HEAD (snapshot)"
  else
    echo "WARN: workflows not all green (re-run with --wait-ci 300)"
  fi
fi

echo ""
echo "=== OpenSSF Scorecard (latest run) ==="
SCORECARD_CONC="$(gh run list --repo "$REPO" --workflow "OpenSSF Scorecard" --limit 1 \
  --json conclusion -q '.[0].conclusion' 2>/dev/null || echo "")"
if [ -z "$SCORECARD_CONC" ] || [ "$SCORECARD_CONC" = "null" ]; then
  if [ "$STRICT" = true ]; then
    echo "FAIL: no Scorecard workflow run found (dispatch scorecard.yml or wait for schedule)"
    ERRORS=$((ERRORS + 1))
  else
    echo "WARN: no Scorecard workflow run found (dispatch scorecard.yml or wait for schedule)"
  fi
elif [ "$SCORECARD_CONC" = "success" ]; then
  echo "OK   Scorecard workflow: success"
else
  echo "FAIL: Scorecard workflow conclusion=${SCORECARD_CONC}"
  ERRORS=$((ERRORS + 1))
fi

if [ "$ERRORS" -gt 0 ]; then
  exit 1
fi
echo "Security triage checks passed"
