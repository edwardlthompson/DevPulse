#!/usr/bin/env bash
# Fail if any tracked file exceeds size budget (matches pre-commit 500KB gate)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

MAX_KB=500
MAX_BYTES=$((MAX_KB * 1024))
ERRORS=0
MAX_REPORT=20
reported=0

# Single ls-tree; parse size without per-file git/awk (Windows Git Bash).
tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT
git ls-tree -r -l HEAD >"$tmp"

while IFS= read -r line; do
  [ -z "$line" ] && continue
  # Format: <mode> <type> <object> <size>\t<path>
  meta="${line%%$'\t'*}"
  path="${line#*$'\t'}"
  [ "$meta" = "$line" ] && continue
  size="${meta##* }"
  case "$size" in
    ''|*[!0-9]*) continue ;;
  esac
  if [ "$size" -gt "$MAX_BYTES" ]; then
    kb=$((size / 1024))
    echo "LARGE TRACKED FILE: $path (${kb} KB > ${MAX_KB} KB)"
    ERRORS=$((ERRORS + 1))
    reported=$((reported + 1))
    if [ "$reported" -ge "$MAX_REPORT" ]; then
      echo "... truncated (max $MAX_REPORT)"
      break
    fi
  fi
done <"$tmp"

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS tracked file(s) exceed ${MAX_KB} KB"
  exit 1
fi

echo "Large tracked file check passed"
