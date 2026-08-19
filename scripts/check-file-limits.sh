#!/usr/bin/env bash
# Enforce file line limits: 300 for static data (UI + i18n), 150 for pure logic
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATIC_DATA_LIMIT=300
LOGIC_LIMIT=150
ERRORS=0

EXCLUDE_PATHS=(
  ! -path "*/node_modules/*"
  ! -path "*/.venv/*"
  ! -path "*/.git/*"
  ! -path "*/dist/*"
  ! -path "*/build/*"
)

check_static_data_paths() {
  local label="$1"
  shift
  while IFS= read -r -d '' file; do
    lines=$(wc -l < "$file" | tr -d ' ')
    if [ "$lines" -gt "$STATIC_DATA_LIMIT" ]; then
      echo "FAIL [$label] $file: $lines lines (max $STATIC_DATA_LIMIT)"
      ERRORS=$((ERRORS + 1))
    fi
  done
}

echo "Checking static data file limits (max $STATIC_DATA_LIMIT lines)..."
check_static_data_paths "static-data" < <(find "$ROOT" -type f \( \
  -name "*.tsx" -o -name "*.jsx" -o -name "*.vue" -o -name "*_view.*" \
  -o -path "*/examples/web/src/components/*.ts" \
  -o -path "*/examples/android/app/src/main/java/*/ui/*/*.kt" \
  -o -path "*/examples/android/app/src/main/java/*/ui/GoldenPath*.kt" \
  -o -path "*/examples/*/locales/*.json" \
  -o -path "*/examples/*/src/locales/*.json" \
  -o -path "*/examples/*/res/values/strings.xml" \
  -o -path "*/examples/*/res/values-*/strings.xml" \
  \) "${EXCLUDE_PATHS[@]}" \
  ! -name "package.json" ! -name "package-lock.json" \
  ! -name "tsconfig.json" ! -name ".lighthouserc.json" \
  -print0 2>/dev/null)

# New scripts/lib/*.py must stay <= 150. Keep this empty unless a split is in flight.
LIB_ALLOWLIST=()

echo "Checking scripts/lib logic file limits (max $LOGIC_LIMIT lines)..."
while IFS= read -r -d '' file; do
  rel="${file#"$ROOT"/}"
  rel="${rel//\\//}"
  skip=false
  for allowed in "${LIB_ALLOWLIST[@]}"; do
    case "$rel" in
      "$allowed"|*/"$allowed") skip=true ;;
    esac
    if [ "$skip" = true ]; then
      break
    fi
  done
  if [ "$skip" = true ]; then
    continue
  fi
  lines=$(wc -l < "$file" | tr -d ' ')
  if [ "$lines" -gt "$LOGIC_LIMIT" ]; then
    echo "FAIL [logic] $file: $lines lines (max $LOGIC_LIMIT)"
    ERRORS=$((ERRORS + 1))
  fi
done < <(find "$ROOT/scripts/lib" -type f -name "*.py" -print0 2>/dev/null)

echo "Checking pure logic file limits (max $LOGIC_LIMIT lines)..."
while IFS= read -r -d '' file; do
  lines=$(wc -l < "$file" | tr -d ' ')
  if [ "$lines" -gt "$LOGIC_LIMIT" ]; then
    echo "FAIL [logic] $file: $lines lines (max $LOGIC_LIMIT)"
    ERRORS=$((ERRORS + 1))
  fi
done < <(find "$ROOT/examples" -type f \( -name "*.ts" -o -name "*.py" -o -name "*.kt" \) \
  ! -name "*.test.*" ! -name "*.spec.*" \
  "${EXCLUDE_PATHS[@]}" \
  ! -path "*/examples/web/src/components/*" \
  ! -path "*/examples/android/app/src/main/java/*/ui/GoldenPath*.kt" \
  ! -path "*/examples/android/app/src/main/java/*/ui/*/*.kt" \
  ! -path "*/examples/web/src/main.ts" \
  -print0 2>/dev/null)

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS file(s) exceed line limits"
  exit 1
fi

echo "All file line limits OK"
