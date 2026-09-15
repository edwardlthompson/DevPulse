"""Audit .cursor/rules/*.mdc alwaysApply vs glob frontmatter."""
from __future__ import annotations

import re
from pathlib import Path

ALLOW = frozenset({
    "main", "core-directives", "cursor-modes", "batch-commands",
    "destructive-ops", "foss-compliance", "commercial-compliance",
    "windows-encoding", "local-compute", "local-deps", "read-before-write",
    "feature-modules", "repo-hygiene", "product-brief",
})
TOGGLE = ("foss-compliance", "commercial-compliance")


def parse_frontmatter(text: str) -> dict[str, object]:
    if not text.startswith("---"):
        return {}
    end = text.find("\n---", 3)
    if end < 0:
        return {}
    block = text[3:end]
    always: bool | None = None
    m = re.search(r"^alwaysApply:\s*(true|false)\s*$", block, re.M)
    if m:
        always = m.group(1) == "true"
    desc = bool(re.search(r"^description:\s*\S", block, re.M))
    globs: list[str] = []
    in_globs = False
    for line in block.splitlines():
        if re.match(r"^globs:\s*$", line):
            in_globs = True
            continue
        if in_globs:
            item = re.match(r"^\s+-\s+(\S.*)$", line)
            if item:
                globs.append(item.group(1).strip().strip("'\""))
                continue
            if re.match(r"^[a-zA-Z]", line):
                in_globs = False
    return {"always": always, "description": desc, "globs": globs}


def audit_rules(root: Path) -> list[str]:
    errors: list[str] = []
    rules = root / ".cursor" / "rules"
    if not rules.is_dir():
        return ["missing .cursor/rules/"]
    flags: dict[str, bool] = {}
    for path in sorted(rules.glob("*.mdc")):
        name = path.stem
        meta = parse_frontmatter(path.read_text(encoding="utf-8"))
        if not meta:
            errors.append(f"{path.name}: missing YAML frontmatter")
            continue
        if not meta["description"]:
            errors.append(f"{path.name}: missing description")
        always = meta["always"]
        globs = list(meta["globs"] or [])
        if always is None:
            errors.append(f"{path.name}: missing alwaysApply")
            continue
        flags[name] = always
        if always:
            if name not in ALLOW:
                errors.append(f"{path.name}: alwaysApply true not on allowlist")
            if globs:
                errors.append(f"{path.name}: alwaysApply true must not set globs")
        elif not globs and name not in TOGGLE:
            errors.append(f"{path.name}: alwaysApply false needs globs")
    foss = flags.get("foss-compliance")
    comm = flags.get("commercial-compliance")
    if foss is True and comm is True:
        errors.append("foss-compliance and commercial-compliance both alwaysApply true")
    if foss is False and comm is False:
        errors.append("foss-compliance and commercial-compliance both alwaysApply false")
    return errors
