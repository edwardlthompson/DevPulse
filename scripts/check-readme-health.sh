#!/usr/bin/env bash
# Automated README health: relative links resolve, markdown tables lint, encoding.
# Usage: scripts/check-readme-health.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ERRORS=0

run_check() {
  if ! "$@"; then
    ERRORS=$((ERRORS + 1))
  fi
}

echo "=== README health check ==="

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

$PY - "$ROOT/README.md" "$ROOT" << 'PY'
import re, sys
from pathlib import Path

readme = Path(sys.argv[1])
root = Path(sys.argv[2])
text = readme.read_text(encoding="utf-8")
errors = []
for m in re.finditer(r'\[[^\]]+\]\(([^)]+)\)', text):
    target = m.group(1).strip()
    if target.startswith(("http://", "https://", "mailto:", "#")):
        continue
    path = (readme.parent / target.split("#")[0]).resolve()
    if not path.exists():
        errors.append(f"broken relative link: {target}")
if errors:
    for e in errors[:20]:
        print(f"FAIL: {e}")
    sys.exit(1)
print("OK   README relative links resolve")
PY

run_check bash scripts/check-markdown-tables.sh
run_check bash scripts/check-file-encoding.sh

# Branding pitch README (mode-gated)
if ! $PY - "$ROOT" << 'PY'
import json
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
product_path = root / "branding" / "product.json"
preview = root / "branding" / "generated" / "README.preview.md"
errors = []

if not product_path.is_file():
    errors.append("missing branding/product.json")
else:
    product = json.loads(product_path.read_text(encoding="utf-8"))
    mode = product.get("mode", "template")
    if not preview.is_file():
        errors.append(
            "missing branding/generated/README.preview.md "
            "(run generate-project-readme.py)"
        )
    else:
        text = preview.read_text(encoding="utf-8")
        for heading in (
            "## Pitch",
            "## Features",
            "## Quick start",
            "## For humans",
            "## For agents",
            "## Install",
            "## Usage",
            "## Contributing",
            "## Security",
            "## License",
        ):
            if heading not in text:
                errors.append(f"preview missing section: {heading}")
        for m in re.finditer(r"\[[^\]]+\]\(([^)]+)\)", text):
            target = m.group(1).strip()
            if target.startswith(("http://", "https://", "mailto:", "#")):
                continue
            path = (preview.parent / target.split("#")[0]).resolve()
            if not path.exists():
                errors.append(f"preview broken link: {target}")

    if mode == "product":
        readme = (root / "README.md").read_text(encoding="utf-8")
        for heading in (
            "## Pitch",
            "## Features",
            "## Quick start",
            "## Contributing",
            "## Security",
            "## License",
        ):
            if heading not in readme:
                errors.append(f"product README missing section: {heading}")

if errors:
    for e in errors[:20]:
        print(f"FAIL: {e}")
    sys.exit(1)
print("OK   Branding README preview / product sections")
PY
then
  ERRORS=$((ERRORS + 1))
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} README health check(s) failed"
  exit 1
fi
echo "README health check passed"
