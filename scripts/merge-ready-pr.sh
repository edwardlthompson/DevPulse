#!/usr/bin/env bash
# Merge a PR once required branch-protection check names are success.
# Usage: scripts/merge-ready-pr.sh PR [--wait SEC]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

WAIT=0
PR=""
while [ $# -gt 0 ]; do
  case "$1" in
    --wait) WAIT="${2:-0}"; shift 2 ;;
    -h|--help)
      echo "Usage: scripts/merge-ready-pr.sh PR [--wait SEC]"
      exit 0
      ;;
    *)
      PR="$1"
      shift
      ;;
  esac
done

if [ -z "$PR" ]; then
  echo "ERROR: PR number required"
  exit 1
fi
if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required"
  exit 1
fi

REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner)"
deadline=$((SECONDS + WAIT))

required_ready() {
  local sha rollup statuses
  sha="$(gh pr view "$PR" --repo "$REPO" --json headRefOid -q .headRefOid)"
  rollup="$(gh pr view "$PR" --repo "$REPO" --json statusCheckRollup -q .statusCheckRollup)"
  statuses="$(gh api "repos/${REPO}/commits/${sha}/status" -q '.statuses')"
  export ROLLUP_JSON="$rollup" STATUS_JSON="$statuses"
  "$PY" - <<'PY'
import json, os, sys
from pathlib import Path

sys.path.insert(0, str(Path("scripts/lib").resolve()))
from required_check_rollup import missing_required

runs = json.loads(os.environ.get("ROLLUP_JSON") or "[]")
statuses = json.loads(os.environ.get("STATUS_JSON") or "[]")
missing = missing_required(runs, statuses=statuses)
if missing:
    print("pending: " + ", ".join(missing))
    raise SystemExit(2)
print("OK   required checks are success")
PY
}

while true; do
  if required_ready; then
    break
  fi
  if [ "$WAIT" -le 0 ] || [ "$SECONDS" -ge "$deadline" ]; then
    echo "FAIL required checks not ready for PR #${PR}"
    exit 1
  fi
  sleep 20
done

state="$(gh pr view "$PR" --repo "$REPO" --json state -q .state)"
if [ "$state" = "MERGED" ]; then
  echo "OK   PR #${PR} already merged"
  exit 0
fi

gh pr merge "$PR" --repo "$REPO" --merge
echo "OK   merged PR #${PR}"
