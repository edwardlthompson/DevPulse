#!/usr/bin/env bash
# Set repo secret AUTOMERGE_TOKEN so Dependabot/Release Please merges trigger push CI.
# Usage:
#   scripts/setup-automerge-token.sh
#   AUTOMERGE_TOKEN=ghp_... scripts/setup-automerge-token.sh
# Default: uses `gh auth token` (needs contents:write + workflow scopes).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required"
  exit 1
fi

TOKEN="${AUTOMERGE_TOKEN:-}"
if [ -z "$TOKEN" ]; then
  TOKEN="$(gh auth token 2>/dev/null || true)"
fi
if [ -z "$TOKEN" ]; then
  echo "ERROR: set AUTOMERGE_TOKEN env or run gh auth login"
  exit 1
fi

gh secret set AUTOMERGE_TOKEN --body "$TOKEN"
echo "OK   AUTOMERGE_TOKEN set on $(gh repo view --json nameWithOwner -q .nameWithOwner)"
echo "Note: classic PATs with workflow scope, or fine-grained tokens with Contents+Workflows, work best."
