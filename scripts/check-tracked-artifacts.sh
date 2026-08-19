#!/usr/bin/env bash
# Fail if forbidden build artifacts or secrets are tracked in git
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! git rev-parse --is-inside-work-tree &>/dev/null; then
  echo "Not a git repository"
  exit 1
fi

ERRORS=0
MAX_REPORT=20
reported=0

check_pattern() {
  local label="$1"
  local pattern="$2"
  while IFS= read -r file; do
    [ -z "$file" ] && continue
    echo "TRACKED FORBIDDEN [$label]: $file"
    ERRORS=$((ERRORS + 1))
    reported=$((reported + 1))
    if [ "$reported" -ge "$MAX_REPORT" ]; then
      echo "... truncated (max $MAX_REPORT)"
      return
    fi
  done < <(git ls-files | grep -E "$pattern" || true)
}

check_pattern "node_modules" 'node_modules/'
check_pattern "dist" '(^|/)dist/'
check_pattern "build" 'examples/android/.*/build/|^build/'
check_pattern "target" '(^|/)target/'
check_pattern "coverage" '(^|/)coverage/'
check_pattern "gradle-cache" '\.gradle/'
check_pattern "pycache" '__pycache__/'
check_pattern "apk" '\.apk$'
check_pattern "aab" '\.aab$'
check_pattern "env-secret" '^\.env$|/\.env$'
check_pattern "exemplar-live-config" \
  'examples/web/public/app-update\.json$|examples/web/public/donations\.json$|examples/android/app/src/main/assets/app-update\.json$|examples/android/app/src/main/assets/donations\.json$'
check_pattern "os-junk" '\.DS_Store$|Thumbs\.db$'

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS forbidden tracked path(s) found"
  exit 1
fi

echo "Tracked artifact check passed"
