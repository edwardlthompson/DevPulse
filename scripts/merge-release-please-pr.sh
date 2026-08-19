#!/usr/bin/env bash
# Merge open Release Please PR (auto-merge queue, then admin fallback for stale/missing checks).
# Usage: scripts/merge-release-please-pr.sh [--wait SEC]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-tools.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-tools.sh"

WAIT=0
while [ $# -gt 0 ]; do
  case "$1" in
    --wait) WAIT="${2:-300}"; shift 2 ;;
    -h|--help)
      echo "Usage: scripts/merge-release-please-pr.sh [--wait SEC]"
      exit 0
      ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required"
  exit 1
fi

PR="$(gh pr list --head release-please--branches--main --state open --json number -q '.[0].number' 2>/dev/null || true)"
if [ -z "$PR" ] || [ "$PR" = "null" ]; then
  PR="$(gh pr list --search "head:release-please--branches--main is:open" --json number -q '.[0].number' 2>/dev/null || true)"
fi

if [ -z "$PR" ] || [ "$PR" = "null" ]; then
  echo "No open Release Please PR found"
  exit 0
fi

echo "Release Please PR #${PR}"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
NOTES="$("$PY" "$ROOT/scripts/lib/changelog_unreleased.py" --fold "$ROOT/CHANGELOG.md")"
if [ -n "$NOTES" ]; then
  if gh pr comment "$PR" --body "$(printf '%s\n\n%s\n' '## Folded [Unreleased] notes' "$NOTES")"; then
    echo "Folded [Unreleased] onto PR #${PR}"
  else
    echo "WARN: could not comment folded [Unreleased] notes on PR #${PR}"
  fi
fi
if ! "$PY" "$ROOT/scripts/lib/changelog_unreleased.py" --require-empty "$ROOT/CHANGELOG.md"; then
  echo "FAIL: empty [Unreleased] before merging Release Please"
  exit 1
fi

if gh pr merge "$PR" --auto --merge 2>/dev/null; then
  echo "Auto-merge queued for PR #${PR}"
else
  echo "WARN: --auto failed; trying direct merge"
fi

SKIP_WAIT="$("$PY" -c "
import json, subprocess, sys
sys.path.insert(0, 'scripts/lib')
from rp_merge_status import skip_auto_merge_wait
raw = subprocess.check_output(
    ['gh', 'pr', 'view', '$PR', '--json', 'statusCheckRollup,mergeStateStatus'],
    text=True,
)
data = json.loads(raw)
print('yes' if skip_auto_merge_wait(data.get('statusCheckRollup'), data.get('mergeStateStatus', '')) else 'no')
")"
if [ "$SKIP_WAIT" = "yes" ]; then
  echo "SKIP wait: PR checks are empty or ACTION_REQUIRED"
  WAIT=0
fi

if [ "$WAIT" -gt 0 ]; then
  echo "Waiting up to ${WAIT}s for merge..."
  end=$((SECONDS + WAIT))
  while [ "$SECONDS" -lt "$end" ]; do
    state="$(gh pr view "$PR" --json state -q .state)"
    if [ "$state" = "MERGED" ]; then
      echo "PR #${PR} merged"
      exit 0
    fi
    sleep 15
  done
fi

state="$(gh pr view "$PR" --json state,mergeStateStatus -q '[.state,.mergeStateStatus] | @tsv')"
if echo "$state" | grep -q "^MERGED"; then
  echo "PR #${PR} already merged"
  exit 0
fi

echo "Admin merge fallback (requires repo admin gh auth)..."
gh pr merge "$PR" --merge --admin
echo "PR #${PR} merged via admin"
