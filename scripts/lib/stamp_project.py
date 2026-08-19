"""Stamp project name/purpose/stack into AGENTS.md (canonical spec)."""
from __future__ import annotations

from pathlib import Path

START = "<!-- bootstrap-project-card -->"
END = "<!-- /bootstrap-project-card -->"


def render_card(name: str, purpose: str, stack: str) -> str:
    return (
        f"{START}\n"
        f"**Product:** {name}\n"
        f"**Purpose:** {purpose}\n"
        f"**Stack:** {stack}\n"
        f"{END}\n"
    )


def stamp_card_file(path: Path, *, name: str, purpose: str, stack: str) -> Path | None:
    if not path.is_file():
        return None
    card = render_card(name, purpose, stack)
    text = path.read_text(encoding="utf-8")
    if START in text and END in text:
        before, rest = text.split(START, 1)
        _old, after = rest.split(END, 1)
        text = before + card + after.lstrip("\n")
    else:
        text = card + "\n" + text
    path.write_text(text, encoding="utf-8")
    return path


def stamp_agents_md(root: Path, *, name: str, purpose: str, stack: str) -> Path | None:
    return stamp_card_file(root / "AGENTS.md", name=name, purpose=purpose, stack=stack)


def stamp_first_30_days(root: Path, *, name: str, purpose: str, stack: str) -> Path | None:
    return stamp_card_file(
        root / "docs" / "FIRST_30_DAYS.md", name=name, purpose=purpose, stack=stack
    )
