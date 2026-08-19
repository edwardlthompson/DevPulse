#!/usr/bin/env bash
# Fail when UI code drifts from the design token system.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ERRORS=0

fail() {
  echo "DESIGN: $1"
  ERRORS=$((ERRORS + 1))
}

if [ ! -f design-tokens/design-tokens.json ]; then
  fail "missing design-tokens/design-tokens.json"
fi

# Branding kit required
for brand_file in \
  branding/BRANDING.md \
  branding/product.json \
  branding/product.schema.json \
  branding/voice.md \
  branding/assets/logo-mark.svg \
  branding/official-colors.css \
  branding/generated/README.preview.md \
  branding/templates/README.product.md
do
  if [ ! -f "$brand_file" ]; then
    fail "missing $brand_file (run scripts/sync-design-tokens.py and scripts/generate-project-readme.py)"
  fi
done

# Hex literals in web UI (allow generated design-tokens.css only)
if [ -d examples/web/src ]; then
  while IFS= read -r -d '' file; do
    if grep -qE '#[0-9A-Fa-f]{6}\b' "$file"; then
      fail "hardcoded hex in $file"
    fi
  done < <(find examples/web/src -type f \( -name '*.css' -o -name '*.ts' \) ! -name 'design-tokens.css' -print0)
fi

# Hex literals in Android UI Kotlin (allow generated Color.kt)
if [ -d examples/android/app/src/main/java ]; then
  while IFS= read -r -d '' file; do
    rel="${file#examples/android/app/src/main/java/}"
    if [[ "$rel" == *"/ui/theme/Color.kt" ]]; then
      continue
    fi
    if [[ "$rel" != *"/ui/"* ]]; then
      continue
    fi
    if grep -qE 'Color\(0x|#[0-9A-Fa-f]{6}\b' "$file"; then
      fail "hardcoded color in $rel"
    fi
  done < <(find examples/android/app/src/main/java -type f -name '*.kt' -print0)
fi

# UI string literals in Compose (allow imports and previews)
if [ -d examples/android/app/src/main/java ]; then
  while IFS= read -r -d '' file; do
    rel="${file#examples/android/app/src/main/java/}"
    if [[ "$rel" != *"/ui/"* ]]; then
      continue
    fi
    if grep -qE 'Text\("[^"]+"\)' "$file"; then
      fail "string literal in composable: $rel"
    fi
  done < <(find examples/android/app/src/main/java -type f -name '*.kt' -print0)
fi

# UI string literals in web main markup
if [ -f examples/web/src/main.ts ]; then
  if grep -qE '<(h1|p|button|span)[^>]*>[^<$]{3,}' examples/web/src/main.ts; then
    fail "main.ts contains hardcoded HTML copy"
  fi
  if ! python3 - "$ROOT/examples/web/src/main.ts" <<'PY'
import re
import sys

path = sys.argv[1]
text = open(path, encoding="utf-8").read()
match = re.search(r"innerHTML\s*=\s*`([^`]*)`", text, re.DOTALL)
if not match:
    sys.exit(0)

template = match.group(1)
if re.search(r">[A-Za-z][^<${}]{3,}<", template):
    sys.exit(1)

for interp in re.findall(r"\$\{([^}]+)\}", template):
    expr = interp.strip()
    if expr.startswith("t("):
        continue
    if re.fullmatch(r"[a-zA-Z_][a-zA-Z0-9_]*", expr):
        continue
    sys.exit(1)
PY
  then
    fail "main.ts innerHTML should use t() or i18n variable keys for visible copy"
  fi
fi

# User-facing copy in CSS content property (allow generated design-tokens.css only)
if [ -d examples/web/src ]; then
  while IFS= read -r -d '' file; do
    if grep -qE "content\s*:\s*['\"][^'\"]{2,}" "$file"; then
      fail "user-facing content property in $file (use locales/*.json)"
    fi
  done < <(find examples/web/src -type f -name '*.css' ! -name 'design-tokens.css' -print0)
fi

# Generated outputs should exist when tokens present and stack is active
REQUIRED_OUTPUTS=(branding/official-colors.css)
if [ -d examples/web ]; then
  REQUIRED_OUTPUTS+=(
    examples/web/src/design-tokens.css
    examples/web/src/theme-meta.json
    examples/web/public/icon.svg
    examples/web/public/favicon.svg
    examples/web/public/logo.svg
  )
fi
if [ -d examples/android ]; then
  REQUIRED_OUTPUTS+=(
    examples/android/app/src/main/java/dev/foss/goldenpath/ui/theme/Color.kt
    examples/android/app/src/main/res/drawable/ic_brand_mark.xml
  )
fi
for out in "${REQUIRED_OUTPUTS[@]}"; do
  if [ ! -f "$out" ]; then
    fail "missing generated output $out (run scripts/sync-design-tokens.py)"
  fi
done

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS design cohesion check(s) failed"
  exit 1
fi

echo "Design cohesion check passed"
