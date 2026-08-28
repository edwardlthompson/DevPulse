#!/usr/bin/env bash
# Fail unless every name=result is success or skipped. Optional commit status.
# Usage:
#   conclude-required-check.sh name=result [name=result ...]
#   JOB_RESULTS=$'a=success\nb=skipped' conclude-required-check.sh
# Env: CONCLUDE_CONTEXT, CONCLUDE_SHA, GH_TOKEN, GH_REPO (optional status post)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

pairs=()
if [ -n "${JOB_RESULTS:-}" ]; then
  while IFS= read -r line; do
    [ -n "$line" ] && pairs+=("$line")
  done <<< "$JOB_RESULTS"
fi
pairs+=("$@")

if [ "${#pairs[@]}" -eq 0 ]; then
  echo "ERROR: pass name=result pairs or JOB_RESULTS"
  exit 1
fi

export CONCLUDE_PAIRS
CONCLUDE_PAIRS="$(printf '%s\n' "${pairs[@]}")"
rc=0
"$PY" - <<'PY' || rc=$?
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path("scripts/lib").resolve()))
from required_check_rollup import jobs_ok, parse_job_pairs

pairs = parse_job_pairs(os.environ["CONCLUDE_PAIRS"].splitlines())
for name, result in pairs.items():
    mark = "OK  " if result in ("success", "skipped") else "FAIL"
    print(f"{mark} {name}: {result}")
raise SystemExit(0 if jobs_ok(pairs) else 1)
PY

state="success"
if [ "$rc" -ne 0 ]; then
  state="failure"
fi

if [ -n "${CONCLUDE_CONTEXT:-}" ] && [ -n "${CONCLUDE_SHA:-}" ] && [ -n "${GH_TOKEN:-}" ]; then
  repo="${GH_REPO:-}"
  if [ -z "$repo" ] && command -v gh >/dev/null 2>&1; then
    repo="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
  fi
  if [ -n "$repo" ]; then
    if gh api --method POST "repos/${repo}/statuses/${CONCLUDE_SHA}" \
      -f state="$state" \
      -f context="$CONCLUDE_CONTEXT" \
      -f description="Required-check rollup" >/dev/null; then
      echo "OK   posted status ${CONCLUDE_CONTEXT}=${state} @ ${CONCLUDE_SHA:0:7}"
    else
      echo "WARN could not post status ${CONCLUDE_CONTEXT}"
    fi
  fi
fi

exit "$rc"
