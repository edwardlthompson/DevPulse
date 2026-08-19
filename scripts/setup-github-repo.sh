#!/usr/bin/env bash
# Idempotent GitHub repo security setup via gh api.
# Enables Dependabot alerts, private vulnerability reporting, and branch protection on main.
# Usage: scripts/setup-github-repo.sh [owner/repo]
# Requires: gh CLI authenticated with admin access to the repo.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-tools.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-tools.sh"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

REPO="${1:-${GITHUB_REPO:-}}"
if [ -z "$REPO" ]; then
  if ! command -v gh >/dev/null 2>&1; then
    echo "ERROR: gh CLI required (https://cli.github.com/)"
    exit 1
  fi
  REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
fi
if [ -z "$REPO" ]; then
  echo "ERROR: pass owner/repo or set GITHUB_REPO, or run from a git repo with gh auth"
  exit 1
fi

BRANCH="${GITHUB_DEFAULT_BRANCH:-main}"
# Comma-separated override: GITHUB_REQUIRED_CHECKS="CI,Security Scan,CodeQL,Repo Hygiene,Feature Gate,Template Upgrade Simulation (Windows)"
if [ -n "${GITHUB_REQUIRED_CHECKS:-}" ]; then
  IFS=',' read -ra REQUIRED_CHECKS <<< "$GITHUB_REQUIRED_CHECKS"
else
  REQUIRED_CHECKS=("CI" "Security Scan" "CodeQL" "Repo Hygiene" "Feature Gate" "Template Upgrade Simulation (Windows)")
fi
TRANSIENT=0
FAILED=0

print_manual_checklist() {
  cat <<'EOF'
MANUAL SETUP CHECKLIST (GitHub UI - API returned 422 or insufficient permissions):
  1. Settings -> Code security and analysis -> Dependabot alerts: ON
  2. Settings -> Code security and analysis -> Dependabot security updates: ON
  3. Settings -> Code security and analysis -> Private vulnerability reporting: ON
  4. Settings -> Branches -> Branch protection rules -> main:
     - Require status checks: CI, Security Scan, CodeQL, Repo Hygiene, Feature Gate, Template Upgrade Simulation (Windows)
     - Require branches to be up to date before merging (recommended)
     - Leave "Do not allow bypassing the above settings" OFF so repo admins can merge via gh --admin
  4b. (Optional, org repos or Rulesets only) Settings -> Rules -> Rulesets -> Bypass list:
     - Add GitHub Actions app, mode "For pull requests only" — not available on classic personal-repo branch rules
  5. Settings -> General -> Features -> Discussions: ON
     Then add a Q&A category (answers enabled) if GitHub did not create one
  5b. Settings → Actions → General: do not require approval for same-repo
     github-actions[bot] / Release Please PRs (required checks stay ACTION_REQUIRED)
  5c. Do not attach GitHub Environments to CI, Security Scan, or CodeQL
     (github-pages on Pages deploy is the exception)
  6. Re-run: bash scripts/setup-github-repo.sh
EOF
}

gh_api_retry() {
  local method="$1"
  local endpoint="$2"
  local data="${3:-}"
  local attempt=1
  local out http_code body rc

  while [ "$attempt" -le 3 ]; do
    if [ -n "$data" ]; then
      out="$(gh api --method "$method" "$endpoint" --input - <<<"$data" -i 2>&1)" || true
    else
      out="$(gh api --method "$method" "$endpoint" -i 2>&1)" || true
    fi

    http_code="$(printf '%s' "$out" | head -1 | awk '{print $2}')"
    body="$(printf '%s' "$out" | tail -n +2)"

    case "$http_code" in
      200|201|204)
        return 0
      ;;
      422)
        echo "SKIP $endpoint (422): $body"
        print_manual_checklist
        return 2
      ;;
      401|403)
        echo "FAIL $endpoint ($http_code): insufficient permissions"
        echo "$body"
        FAILED=$((FAILED + 1))
        return 1
      ;;
      404)
        echo "FAIL $endpoint (404): not found"
        echo "$body"
        FAILED=$((FAILED + 1))
        return 1
      ;;
      000|"")
        echo "RETRY $endpoint (attempt $attempt): network or gh error"
        echo "$out"
        TRANSIENT=$((TRANSIENT + 1))
      ;;
      *)
        if [ "${http_code:-0}" -ge 500 ]; then
          echo "RETRY $endpoint ($http_code) attempt $attempt"
          TRANSIENT=$((TRANSIENT + 1))
        else
          echo "FAIL $endpoint ($http_code)"
          echo "$body"
          FAILED=$((FAILED + 1))
          return 1
        fi
      ;;
    esac
    attempt=$((attempt + 1))
    sleep $((attempt * 2))
  done
  return 3
}

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required (https://cli.github.com/)"
  exit 1
fi

echo "Setting up GitHub repo security for ${REPO} (branch: ${BRANCH})"

rc=0
if ! gh_api_retry PUT "repos/${REPO}/vulnerability-alerts"; then
  rc=$?
  if [ "$rc" -eq 3 ]; then TRANSIENT=$((TRANSIENT + 1)); elif [ "$rc" -eq 1 ]; then FAILED=$((FAILED + 1)); fi
else
  echo "OK   Dependabot vulnerability alerts enabled"
fi

if ! gh_api_retry PUT "repos/${REPO}/private-vulnerability-reporting"; then
  rc=$?
  if [ "$rc" -eq 3 ]; then TRANSIENT=$((TRANSIENT + 1)); elif [ "$rc" -eq 1 ]; then FAILED=$((FAILED + 1)); fi
else
  echo "OK   Private vulnerability reporting enabled"
fi

export GITHUB_REQUIRED_CHECKS="$(IFS=,; echo "${REQUIRED_CHECKS[*]}")"

protection_json="$("$PY" - <<PY
import json, os
checks = os.environ.get("GITHUB_REQUIRED_CHECKS", "CI,Security Scan,CodeQL,Repo Hygiene,Feature Gate,Template Upgrade Simulation (Windows)").split(",")
checks = [c.strip() for c in checks if c.strip()]
print(json.dumps({
    "required_status_checks": {"strict": True, "contexts": checks},
    "enforce_admins": False,
    "required_pull_request_reviews": {
        "dismiss_stale_reviews": True,
        "require_code_owner_reviews": False,
        "required_approving_review_count": 0,
    },
    "bypass_pull_request_allowances": {
        "users": [],
        "teams": [],
        "apps": ["github-actions"],
    },
    "restrictions": None,
    "required_linear_history": False,
    "allow_force_pushes": False,
    "allow_deletions": False,
    "block_creations": False,
}))
PY
)"

if ! gh_api_retry PUT "repos/${REPO}/branches/${BRANCH}/protection" "$protection_json"; then
  rc=$?
  if [ "$rc" -eq 3 ]; then TRANSIENT=$((TRANSIENT + 1)); elif [ "$rc" -eq 1 ]; then FAILED=$((FAILED + 1)); fi
else
  echo "OK   Branch protection on ${BRANCH} (required checks: ${REQUIRED_CHECKS[*]})"
fi

if gh repo edit "$REPO" --enable-discussions >/dev/null 2>&1; then
  echo "OK   GitHub Discussions enabled (point Q&A at SUPPORT.md)"
else
  echo "SKIP Discussions (gh could not enable — [HUMAN] Settings → General → Features → Discussions)"
fi

warn_required_check_environments() {
  echo "NOTE: Do not attach GitHub Environments to CI, Security Scan, or CodeQL."
  local names
  names="$(gh api "repos/${REPO}/environments" --jq '.environments[].name' 2>/dev/null || true)"
  if [ -n "$names" ]; then
    echo "NOTE: Environments present: $(printf '%s' "$names" | tr '\n' ' ')"
    echo "[HUMAN] Settings → Environments: reviewers on github-pages are OK; do not gate CI / Security Scan / CodeQL"
  fi
  echo "[HUMAN] Settings → Actions → General: do not require approval for same-repo github-actions[bot] / Release Please PRs"
}

ensure_discussions_qa() {
  "$PY" "$ROOT/scripts/lib/discussions_qa.py" "$REPO"
}

warn_required_check_environments
ensure_discussions_qa

if [ "$TRANSIENT" -gt 0 ]; then
  echo "Transient errors after retries ($TRANSIENT); re-run later"
  exit 2
fi
if [ "$FAILED" -gt 0 ]; then
  echo "$FAILED setup step(s) failed"
  exit 1
fi

echo "GitHub repo security setup complete for ${REPO}"
