#!/usr/bin/env bash
# Orchestrate template-maintainer weekly/monthly/milestone AUTO gates.
# Usage: scripts/run-maintainer-gates.sh [--quick] [--wait-ci SEC] [--skip-apk]
#   --quick     Skip pre-release gate; poll CI without --wait (snapshot only)
#   --wait-ci   Seconds to wait for CI in full mode (default 300)
#   --skip-apk  Skip verify-reproducible-apk.sh in full mode
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

QUICK=false
SKIP_APK=false
WAIT_CI=300
while [ $# -gt 0 ]; do
  case "$1" in
    --quick) QUICK=true; shift ;;
    --wait-ci) WAIT_CI="${2:-300}"; shift 2 ;;
    --skip-apk) SKIP_APK=true; shift ;;
    -h|--help)
      echo "Usage: scripts/run-maintainer-gates.sh [--quick] [--wait-ci SEC] [--skip-apk]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      echo "Usage: scripts/run-maintainer-gates.sh [--quick] [--wait-ci SEC] [--skip-apk]" >&2
      exit 1
      ;;
  esac
done

ERRORS=0

run_step() {
  local name="$1"
  shift
  echo ""
  echo "=== ${name} ==="
  if "$@"; then
    echo "OK   ${name}"
  else
    echo "FAIL ${name}"
    ERRORS=$((ERRORS + 1))
  fi
}

run_step "readme-health" bash scripts/check-readme-health.sh
run_step "fdroid-metadata" bash scripts/verify-fdroid-metadata.sh
if command -v gh >/dev/null 2>&1; then
  run_step "branch-protection" bash scripts/verify-branch-protection.sh
fi
run_step "simulate-upgrade" bash scripts/simulate-template-upgrade.sh
if [ "$QUICK" = true ]; then
  run_step "feature-gate" bash scripts/feature-gate.sh --stack multi --strict
fi

if [ "$QUICK" = true ]; then
  run_step "security-triage" bash scripts/check-security-triage.sh
  if command -v gh >/dev/null 2>&1; then
    run_step "ci-jobs" bash scripts/check-github-ci.sh HEAD --skip-workflows --jobs "Repo Hygiene,Feature Gate,Template Upgrade Simulation (Windows)"
  fi
else
  run_step "pre-release" bash scripts/pre-release-gate.sh
  if [ "$SKIP_APK" = false ] && [ -f examples/android/gradlew ]; then
    run_step "reproducible-apk" bash scripts/verify-reproducible-apk.sh --strict
  fi
  if command -v gh >/dev/null 2>&1; then
    run_step "ci-jobs" bash scripts/check-github-ci.sh HEAD --wait "$WAIT_CI" --skip-workflows --jobs "Repo Hygiene,Feature Gate,Template Upgrade Simulation (Windows)"
  fi
fi

echo ""
if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} maintainer gate(s) failed"
  exit 1
fi

echo "All maintainer gates passed"
