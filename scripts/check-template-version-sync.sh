#!/usr/bin/env bash
# Fail when .template-version drifts from TEMPLATE_INDEX (and, on the template repo,
# from release-please / CITATION / plugin).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ ! -f .release-please-manifest.json ] || [ ! -f .template-version ]; then
  echo "MISSING: version manifest or .template-version"
  exit 1
fi

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

VERSION="$(tr -d '[:space:]' < .template-version)"
IS_TEMPLATE="$("$PY" -c "from pathlib import Path; from scripts.lib.build_sprint_model import is_template_repo; print('1' if is_template_repo(Path('.')) else '0')")"

IDX="$("$PY" -c "import json; print(json.load(open('TEMPLATE_INDEX.json'))['template_version'])")"
if [ "$IDX" != "$VERSION" ]; then
  echo "FAIL: TEMPLATE_INDEX template_version ($IDX) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

if [ "$IS_TEMPLATE" != "1" ]; then
  # Child: .template-version tracks upstream template; app version stays in release-please.
  MANIFEST="$("$PY" -c "import json; print(json.load(open('.release-please-manifest.json'))['.'].strip())")"
  echo "Template version sync OK (child template=$VERSION app=$MANIFEST)"
  exit 0
fi

MANIFEST="$("$PY" -c "import json; print(json.load(open('.release-please-manifest.json'))['.'].strip())")"
if [ "$MANIFEST" != "$VERSION" ]; then
  echo "FAIL: .template-version ($VERSION) != manifest ($MANIFEST)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

CFF="$("$PY" -c "
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

CFF_DATE="$("$PY" -c "
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

PLUGIN="$("$PY" -c "import json; print(json.load(open('.cursor-plugin/plugin.json', encoding='utf-8')).get('version',''))")"
if [ "$PLUGIN" != "$VERSION" ]; then
  echo "FAIL: .cursor-plugin/plugin.json version ($PLUGIN) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

RP_PLUGIN="$("$PY" -c "
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
