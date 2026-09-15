#!/usr/bin/env bash
# Fail when .template-version drifts from .release-please-manifest.json
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ ! -f .release-please-manifest.json ] || [ ! -f .template-version ]; then
  echo "MISSING: version manifest or .template-version"
  exit 1
fi

MANIFEST="$(python3 -c "import json; print(json.load(open('.release-please-manifest.json'))['.'].strip())")"
VERSION="$(tr -d '[:space:]' < .template-version)"

if [ "$MANIFEST" != "$VERSION" ]; then
  echo "FAIL: .template-version ($VERSION) != manifest ($MANIFEST)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

IDX="$(python3 -c "import json; print(json.load(open('TEMPLATE_INDEX.json'))['template_version'])")"
if [ "$IDX" != "$VERSION" ]; then
  echo "FAIL: TEMPLATE_INDEX template_version ($IDX) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

CFF="$(python3 -c "
import re
from pathlib import Path
text = Path('CITATION.cff').read_text(encoding='utf-8')
m = re.search(r'(?m)^version:\\s*([\\d.]+)', text)
print(m.group(1) if m else '')
")"
if [ -z "$CFF" ] || [ "$CFF" != "$VERSION" ]; then
  echo "FAIL: CITATION.cff version ($CFF) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

CFF_DATE="$(python3 -c "
import re
from pathlib import Path
text = Path('CITATION.cff').read_text(encoding='utf-8')
m = re.search(r'(?m)^date-released:\\s*(\\d{4}-\\d{2}-\\d{2})', text)
print(m.group(1) if m else '')
")"
if [ -z "$CFF_DATE" ]; then
  echo "FAIL: CITATION.cff missing date-released: YYYY-MM-DD"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

PLUGIN="$(python3 -c "import json; print(json.load(open('.cursor-plugin/plugin.json', encoding='utf-8')).get('version',''))")"
if [ "$PLUGIN" != "$VERSION" ]; then
  echo "FAIL: .cursor-plugin/plugin.json version ($PLUGIN) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

RP_PLUGIN="$(python3 -c "
import json
cfg = json.load(open('release-please-config.json', encoding='utf-8'))
ok = False
for item in cfg['packages']['.']['extra-files']:
    if isinstance(item, dict) and item.get('path') == '.cursor-plugin/plugin.json' and item.get('jsonpath') == '\$.version':
        ok = True
print('ok' if ok else '')
")"
if [ "$RP_PLUGIN" != "ok" ]; then
  echo "FAIL: release-please-config extra-files must bump .cursor-plugin/plugin.json \$.version"
  exit 1
fi

echo "Template version sync OK ($VERSION)"
