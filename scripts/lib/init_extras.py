"""Optional init writers: FUNDING.yml and GitHub About topics."""
from __future__ import annotations

import re
from pathlib import Path

PLACEHOLDER_RE = re.compile(r"\[INSERT[^\]]*\]", re.I)


def donation_url_usable(url: str) -> bool:
    text = (url or "").strip()
    if not text or PLACEHOLDER_RE.search(text):
        return False
    return text.startswith(("https://", "http://"))


def write_funding_yml(root: Path, url: str) -> Path | None:
    if not donation_url_usable(url):
        return None
    dest = root / ".github" / "FUNDING.yml"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(f"custom:\n  - {url.strip()}\n", encoding="utf-8")
    return dest


def merge_topics(about_text: str, topics: list[str]) -> str:
    clean = [t.strip().lower().replace(" ", "-") for t in topics if t.strip()]
    if not clean:
        return about_text
    line = ", ".join(clean)
    if "## Topics" in about_text:
        before, rest = about_text.split("## Topics", 1)
        after = rest.split("\n", 1)[1] if "\n" in rest else ""
        # keep remainder after the first paragraph
        parts = after.split("\n\n", 1)
        tail = parts[1] if len(parts) > 1 else ""
        mid = f"## Topics\n\n{line}\n\nSuggested for GitHub discoverability (Settings → About).\n"
        return before + mid + (("\n" + tail) if tail else "")
    return about_text.rstrip() + f"\n\n## Topics\n\n{line}\n"


def write_topics(root: Path, topics: list[str]) -> Path | None:
    path = root / "docs" / "GITHUB_ABOUT.md"
    if not path.is_file() or not topics:
        return None
    text = merge_topics(path.read_text(encoding="utf-8"), topics)
    path.write_text(text, encoding="utf-8")
    return path


def gh_topics_command(topics: list[str]) -> str:
    clean = [t.strip().lower().replace(" ", "-") for t in topics if t.strip()]
    if not clean:
        return ""
    joined = ",".join(clean)
    return f"gh repo edit --add-topic {joined}"
