#!/usr/bin/env bash
# Verify third-party action refs in .github/workflows resolve on GitHub.
# Catches invalid tags like aquasecurity/trivy-action@0.28.0 (should be v0.36.0 or SHA).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required (https://cli.github.com/)"
  exit 1
fi

if [ -z "${GH_TOKEN:-${GITHUB_TOKEN:-}}" ]; then
  echo "WARN: GH_TOKEN/GITHUB_TOKEN not set; gh api may rate-limit or fail"
fi

mapfile -t USES_LINES < <(grep -rhoE 'uses:[[:space:]]*[^[:space:]]+/[^@[:space:]]+@[^[:space:]]+' .github/workflows/*.yml \
  | sed -E 's/^uses:[[:space:]]*//' | sort -u)

if [ "${#USES_LINES[@]}" -eq 0 ]; then
  echo "No workflow action refs found"
  exit 0
fi

ERRORS=0

gh_ref_exists() {
  gh api "$1" >/dev/null 2>&1
}

action_repo() {
  local action_path="$1"
  local parts count
  IFS=/ read -ra parts <<< "$action_path"
  count="${#parts[@]}"
  if [ "$count" -gt 2 ]; then
    printf '%s/%s' "${parts[0]}" "${parts[1]}"
  else
    printf '%s' "$action_path"
  fi
}

resolve_ref() {
  local repo="$1" ref="$2"
  if gh_ref_exists "repos/${repo}/git/ref/tags/${ref}"; then return 0; fi
  if gh_ref_exists "repos/${repo}/git/ref/heads/${ref}"; then return 0; fi
  if [[ "${ref}" =~ ^[0-9a-f]{7,40}$ ]] && gh_ref_exists "repos/${repo}/commits/${ref}"; then return 0; fi
  if [[ "${ref}" =~ ^[0-9a-f]{7,40}$ ]] && gh_ref_exists "repos/${repo}/contents/README.md?ref=${ref}"; then return 0; fi
  if [[ "${ref}" =~ ^v ]]; then return 1; fi
  if [[ "${ref}" =~ ^[0-9] ]] && gh_ref_exists "repos/${repo}/git/ref/tags/v${ref}"; then
    echo "HINT: ${repo}@${ref} missing — did you mean v${ref}?"
    return 1
  fi
  return 1
}

for entry in "${USES_LINES[@]}"; do
  action_path="${entry%@*}"
  ref="${entry#*@}"
  repo="$(action_repo "$action_path")"
  if resolve_ref "$repo" "$ref"; then
    echo "OK   ${action_path}@${ref}"
  else
    echo "FAIL ${action_path}@${ref} (repo ${repo}, ref ${ref} not found on GitHub)"
    ERRORS=$((ERRORS + 1))
  fi
done

if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} invalid workflow action ref(s)"
  exit 1
fi

echo "All ${#USES_LINES[@]} workflow action ref(s) resolve"
