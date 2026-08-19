#!/usr/bin/env bash
# Fail on bare semver GitHub Action refs (e.g. @0.28.0) in workflow files.
# Use @vX.Y.Z or a full commit SHA instead.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

shopt -s nullglob
files=(.github/workflows/*.yml .github/workflows/*.yaml)
shopt -u nullglob

if [ ${#files[@]} -eq 0 ]; then
  echo "No workflow files found"
  exit 0
fi

if grep -nE 'uses:[[:space:]]*[^[:space:]]+@[0-9]+\.[0-9]' "${files[@]}"; then
  echo ""
  echo "ERROR: Bare semver action refs detected (missing v prefix)."
  echo "Use @vX.Y.Z or a full commit SHA, e.g.:"
  echo "  uses: aquasecurity/trivy-action@a9c7b0f06e461e9d4b4d1711f154ee024b8d7ab8 # v0.36.0"
  exit 1
fi

echo "Workflow action ref format check passed"
