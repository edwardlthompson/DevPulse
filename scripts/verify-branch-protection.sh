#!/usr/bin/env bash
# Verify GitHub branch protection required contexts match setup-github-repo.sh defaults.
# Usage: scripts/verify-branch-protection.sh [owner/repo] [--branch BRANCH]
# Env: GITHUB_REPO, GITHUB_DEFAULT_BRANCH, GITHUB_REQUIRED_CHECKS (comma-separated)
# Exit 0 when all expected checks are present; 1 on mismatch or API error.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-tools.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-tools.sh"

REPO="${GITHUB_REPO:-}"
BRANCH="${GITHUB_DEFAULT_BRANCH:-main}"

while [ $# -gt 0 ]; do
  case "$1" in
    --branch) BRANCH="${2:-main}"; shift 2 ;;
    -h|--help)
      echo "Usage: scripts/verify-branch-protection.sh [owner/repo] [--branch BRANCH]"
      exit 0
      ;;
    *)
      REPO="$1"
      shift
      ;;
  esac
done

if [ -z "$REPO" ]; then
  if ! command -v gh >/dev/null 2>&1; then
    echo "ERROR: gh CLI required (https://cli.github.com/)"
    exit 1
  fi
  REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
fi
if [ -z "$REPO" ]; then
  echo "ERROR: pass owner/repo, set GITHUB_REPO, or run from a git repo with gh auth"
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required"
  exit 1
fi

if [ -n "${GITHUB_REQUIRED_CHECKS:-}" ]; then
  EXPECTED_CSV="$GITHUB_REQUIRED_CHECKS"
else
  EXPECTED_CSV="CI,Security Scan,CodeQL,Repo Hygiene,Feature Gate,Template Upgrade Simulation (Windows)"
fi

echo "Branch protection for ${REPO} @ ${BRANCH}"
echo "Expected required checks: ${EXPECTED_CSV}"

if ! protection_json="$(gh api "repos/${REPO}/branches/${BRANCH}/protection" 2>/dev/null)"; then
  echo "FAIL: could not read branch protection (missing rule or insufficient permissions)"
  echo "Run: bash scripts/setup-github-repo.sh ${REPO}"
  exit 1
fi

export EXPECTED_CSV REPO PROTECTION_JSON="$protection_json"
python3 - <<'PY'
import json, os, sys

expected = {c.strip() for c in os.environ["EXPECTED_CSV"].split(",") if c.strip()}
raw = os.environ.get("PROTECTION_JSON", "")
if not raw.strip():
    print("FAIL: empty branch protection response")
    raise SystemExit(1)
data = json.loads(raw)
contexts = set(data.get("required_status_checks", {}).get("contexts", []))
strict = data.get("required_status_checks", {}).get("strict", False)
force_enabled = data.get("allow_force_pushes", {}).get("enabled", True)
missing = sorted(expected - contexts)
extra = sorted(contexts - expected)

for ctx in sorted(contexts & expected):
    print(f"OK   required check: {ctx}")
for ctx in missing:
    print(f"FAIL missing required check: {ctx}")
for ctx in extra:
    print(f"WARN extra check (not in template default): {ctx}")

if not strict:
    print("FAIL required_status_checks.strict is false (stale commits may pass)")
if force_enabled:
    print("FAIL allow_force_pushes is enabled on protected branch")

if missing or not strict or force_enabled:
    repo = os.environ.get("REPO", "owner/repo")
    print("")
    print("Fix: export GITHUB_REQUIRED_CHECKS if workflow names differ, then:")
    print(f"  bash scripts/setup-github-repo.sh {repo}")
    raise SystemExit(1)
PY

echo "All expected branch protection checks are configured"
