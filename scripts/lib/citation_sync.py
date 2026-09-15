"""Keep CITATION.cff version and date-released in YYYY-MM-DD form."""
from __future__ import annotations

import re
from datetime import date
from pathlib import Path


def sync_citation(text: str, version: str, released: str | None = None) -> str:
    day = released or date.today().isoformat()
    updated, n = re.subn(r"(?m)^version:\s*\S+", f"version: {version}", text, count=1)
    if n:
        text = updated
    if re.search(r"(?m)^date-released:\s*", text):
        text = re.sub(
            r"(?m)^date-released:\s*\S+",
            f"date-released: {day}",
            text,
            count=1,
        )
    else:
        text = text.rstrip() + f"\ndate-released: {day}\n"
    return text


def write_citation(path: Path, version: str, released: str | None = None) -> None:
    if not path.is_file():
        return
    path.write_text(
        sync_citation(path.read_text(encoding="utf-8"), version, released),
        encoding="utf-8",
        newline="\n",
    )
