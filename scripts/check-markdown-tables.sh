#!/usr/bin/env bash
# Fail when a GFM table row is separated from the next row by a blank line.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

TARGETS=(README.md BUILD_PLAN.md)
ERRORS=0

for file in "${TARGETS[@]}"; do
  if [ ! -f "$file" ]; then
    echo "SKIP missing: $file"
    continue
  fi
  if grep -Pzq '\|\s*\n\n\|' "$file"; then
    echo "BROKEN GFM TABLE: blank line between rows in $file"
    ERRORS=$((ERRORS + 1))
  fi
done

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS markdown table check(s) failed"
  exit 1
fi

echo "Markdown table check passed"
