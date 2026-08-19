#!/usr/bin/env bash
# Poll GitHub Actions for required workflows on a commit.
# Usage: scripts/check-github-ci.sh [REF] [--wait SECONDS] [--jobs JOB1,JOB2] [--skip-workflows] [--dispatch-if-missing]
#   --skip-workflows       Poll only --jobs (no CI/Security Scan/CodeQL rollup); requires --jobs
#   --dispatch-if-missing  workflow_dispatch required workflows that have no run on REF yet
# Requires: gh CLI authenticated to the repo.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

REF="HEAD"
WAIT=0
JOBS_CSV=""
SKIP_WORKFLOWS=0
DISPATCH_IF_MISSING=0
while [ $# -gt 0 ]; do
  case "$1" in
    --wait) WAIT="${2:-300}"; shift 2 ;;
    --jobs) JOBS_CSV="${2:-}"; shift 2 ;;
    --skip-workflows) SKIP_WORKFLOWS=1; shift ;;
    --dispatch-if-missing) DISPATCH_IF_MISSING=1; shift ;;
    *)
      REF="$1"
      shift
      ;;
  esac
done
REF="$(git rev-parse "$REF")"

if [ "$SKIP_WORKFLOWS" -eq 1 ] && [ -z "$JOBS_CSV" ]; then
  echo "ERROR: --skip-workflows requires --jobs"
  exit 1
fi

REQUIRED=("CI" "Security Scan" "CodeQL")

workflow_file_for() {
  case "$1" in
    CI) echo "ci.yml" ;;
    "Security Scan") echo "security.yml" ;;
    CodeQL) echo "codeql.yml" ;;
    *) echo "" ;;
  esac
}

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required (https://cli.github.com/)"
  exit 1
fi

REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
if [ -z "$REPO" ]; then
  echo "ERROR: run from a git repo with gh auth, or export GITHUB_REPO=owner/name"
  exit 1
fi

# Resolve branch tip for workflow_dispatch when REF is that tip (dispatch needs a ref name).
DISPATCH_REF="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo main)"
if [ "$DISPATCH_REF" = "HEAD" ]; then
  DISPATCH_REF="main"
fi

echo "GitHub Actions status for ${REPO} @ ${REF:0:7}"

has_run_for_workflow() {
  local wf="$1"
  gh run list --repo "$REPO" --commit "$REF" --workflow "$wf" --limit 1 \
    --json databaseId -q '.[0].databaseId' 2>/dev/null | grep -q .
}

dispatch_missing() {
  [ "$DISPATCH_IF_MISSING" -eq 1 ] || return 0
  [ "$SKIP_WORKFLOWS" -eq 1 ] && return 0
  local wf file
  for wf in "${REQUIRED[@]}"; do
    if has_run_for_workflow "$wf"; then
      continue
    fi
    file="$(workflow_file_for "$wf")"
    if [ -z "$file" ]; then
      echo "WARN cannot dispatch ${wf}: no workflow file mapping"
      continue
    fi
    echo "DISPATCH ${wf}: no run on ${REF:0:7}; workflow_dispatch ${file} @ ${DISPATCH_REF}"
    if ! gh workflow run "$file" --repo "$REPO" --ref "$DISPATCH_REF"; then
      echo "WARN dispatch failed for ${wf} (${file})"
    fi
  done
}

dispatch_missing

deadline=$((SECONDS + WAIT))
while true; do
  mapfile -t RUNS < <(gh run list --repo "$REPO" --commit "$REF" \
    --json workflowName,conclusion,status,url -q '.[] | [.workflowName,.status,.conclusion,.url] | @tsv')

  PENDING=0
  FAILED=0

  for wf in "${REQUIRED[@]}"; do
    if [ "$SKIP_WORKFLOWS" -eq 1 ]; then
      break
    fi
    wf_lines="$(printf '%s\n' "${RUNS[@]}" | grep "^${wf}"$'\t' || true)"
    if [ -z "$wf_lines" ]; then
      echo "WAIT ${wf}: no run yet"
      PENDING=$((PENDING + 1))
      continue
    fi
    line="$(printf '%s\n' "$wf_lines" | awk -F'\t' '$3=="success" {print; exit}')"
    if [ -z "$line" ]; then
      line="$(printf '%s\n' "$wf_lines" | awk -F'\t' '$2!="completed" {print; exit}')"
    fi
    if [ -z "$line" ]; then
      line="$(printf '%s\n' "$wf_lines" | head -1)"
    fi
    IFS=$'\t' read -r _ status conclusion url <<<"$line"
    case "$conclusion" in
      success) echo "OK   ${wf}: ${url}" ;;
      failure|cancelled|timed_out|action_required)
        echo "FAIL ${wf} (${conclusion}): ${url}"
        FAILED=$((FAILED + 1))
        ;;
      *)
        if [ "$status" = "completed" ]; then
          echo "FAIL ${wf} (${conclusion:-unknown}): ${url}"
          FAILED=$((FAILED + 1))
        else
          echo "WAIT ${wf} (${status}): ${url}"
          PENDING=$((PENDING + 1))
        fi
        ;;
    esac
  done

  if [ -n "$JOBS_CSV" ]; then
    RUN_ID="$(gh run list --repo "$REPO" --commit "$REF" --workflow CI --json databaseId -q '.[0].databaseId' 2>/dev/null || true)"
    if [ -z "$RUN_ID" ]; then
      echo "WAIT CI jobs: no CI run yet"
      PENDING=$((PENDING + 1))
    else
      IFS=',' read -ra JOBS <<< "$JOBS_CSV"
      for job in "${JOBS[@]}"; do
        job="$(echo "$job" | xargs)"
        [ -z "$job" ] && continue
        conclusion="$(gh run view "$RUN_ID" --repo "$REPO" --json jobs \
          -q ".jobs[] | select(.name == \"${job}\") | .conclusion" 2>/dev/null | head -1 || true)"
        case "$conclusion" in
          success) echo "OK   CI job: ${job}" ;;
          "") echo "WAIT CI job: ${job} (not found)"; PENDING=$((PENDING + 1)) ;;
          *) echo "FAIL CI job ${job} (${conclusion})"; FAILED=$((FAILED + 1)) ;;
        esac
      done
    fi
  fi

  if [ "$FAILED" -gt 0 ]; then
    echo "${FAILED} required workflow(s) failed on GitHub"
    exit 1
  fi
  if [ "$PENDING" -eq 0 ]; then
    echo "All required GitHub checks passed"
    exit 0
  fi
  if [ "$WAIT" -eq 0 ] || [ "$SECONDS" -ge "$deadline" ]; then
    echo "INCOMPLETE: ${PENDING} workflow(s)/job(s) still pending (re-run with --wait 300)"
    exit 1
  fi
  sleep 15
done
