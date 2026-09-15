#!/usr/bin/env bash
# Sync .template-version, TEMPLATE_INDEX, and README badge from .release-please-manifest.json
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ ! -f .release-please-manifest.json ]; then
  echo "MISSING: .release-please-manifest.json"
  exit 1
fi

VERSION="$(python3 -c "import json; print(json.load(open('.release-please-manifest.json'))['.'].strip())")"

if [ -z "$VERSION" ]; then
  echo "FAIL: empty version in manifest"
  exit 1
fi

echo "$VERSION" > .template-version

export SYNC_TEMPLATE_VERSION="${VERSION}"
python3 <<'PY'
import json
import os
import re
import sys
from pathlib import Path

version = os.environ["SYNC_TEMPLATE_VERSION"]
idx = Path("TEMPLATE_INDEX.json")
data = json.loads(idx.read_text(encoding="utf-8"))
data["template_version"] = version
idx.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")

readme = Path("README.md")
text = readme.read_text(encoding="utf-8")
text = re.sub(
    r"!\[Template\]\(https://img\.shields\.io/badge/template-[\d.]+",
    f"![Template](https://img.shields.io/badge/template-{version}",
    text,
)
text = re.sub(
    r"Current template version: \*\*[\d.]+\*\*",
    f"Current template version: **{version}**",
    text,
)
readme.write_text(text, encoding="utf-8")

mem = Path("AGENT_MEMORY.md")
mt = mem.read_text(encoding="utf-8")
mt = re.sub(
    r"(\| Multi-stack template[^\|]+\| )[\d.]+( \| Template maintainer)",
    lambda m: f"{m.group(1)}{version}{m.group(2)}",
    mt,
)
mt = re.sub(
    r"(\*\*Template version:\*\* `)[\d.]+(`)",
    lambda m: f"{m.group(1)}{version}{m.group(2)}",
    mt,
)
mem.write_text(mt, encoding="utf-8")

plugin = Path(".cursor-plugin/plugin.json")
if plugin.is_file():
    pdata = json.loads(plugin.read_text(encoding="utf-8"))
    pdata["version"] = version
    plugin.write_text(
        json.dumps(pdata, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
        newline="\n",
    )

citation = Path("CITATION.cff")
if citation.is_file():
    sys.path.insert(0, str(Path("scripts/lib").resolve()))
    from citation_sync import write_citation
    write_citation(citation, version)
PY

echo "Synced template version to ${VERSION} (.template-version, TEMPLATE_INDEX.json, README.md, AGENT_MEMORY.md, CITATION.cff, .cursor-plugin/plugin.json)"
python3 "$ROOT/scripts/lib/changelog_unreleased.py" --move-first "$ROOT/CHANGELOG.md"
