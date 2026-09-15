"""Stamp Product (do not drift) blocks from AGENT.md. Never writes AGENT.md."""
from __future__ import annotations

from pathlib import Path

from agent_brief import BEGIN, EMPTY_NOTE, END, parse_brief, render_inner, replace_inner
from build_sprint_model import is_template_repo

TARGETS = (
    "BUILD_PLAN.md",
    "docs/spec.md",
    "AGENT_MEMORY.md",
)


def stamp_file(path: Path, inner: str) -> bool:
    if not path.is_file():
        return False
    raw = path.read_text(encoding="utf-8")
    if BEGIN not in raw or END not in raw:
        return False
    updated = replace_inner(raw, inner)
    if updated == raw:
        return False
    path.write_text(updated, encoding="utf-8", newline="\n")
    return True


def stamp_root(root: Path) -> list[Path]:
    agent = root / "AGENT.md"
    if agent.is_file():
        brief = parse_brief(agent.read_text(encoding="utf-8"))
        inner = render_inner(brief)
    elif is_template_repo(root):
        inner = EMPTY_NOTE
    else:
        return []
    written: list[Path] = []
    for rel in TARGETS:
        path = root / rel
        if stamp_file(path, inner):
            written.append(path)
    return written
