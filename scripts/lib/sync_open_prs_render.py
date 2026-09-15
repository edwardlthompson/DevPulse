"""Classify and render Dependabot / Release Please rows for BUILD_PLAN sync."""
from __future__ import annotations

import re
from typing import Any

BEGIN = "<!-- open-prs-sync:begin -->"
END = "<!-- open-prs-sync:end -->"
EMPTY_NOTE = "_No open Dependabot or Release Please PRs._"
RELEASE_TITLE = re.compile(r"^chore(\([^)]*\))?:\s*release\b", re.I)


def classify_pr(pr: dict[str, Any]) -> str | None:
    """Return 'dependabot', 'release', or None."""
    author = ((pr.get("author") or {}).get("login") or "").lower()
    labels = {str((lab or {}).get("name") or "").lower() for lab in (pr.get("labels") or [])}
    head = str(pr.get("headRefName") or "")
    title = str(pr.get("title") or "")
    if author in {"dependabot[bot]", "dependabot"} or "dependencies" in labels:
        return "dependabot"
    if head.startswith("release-please") or RELEASE_TITLE.match(title.strip()):
        return "release"
    return None


def format_row(pr: dict[str, Any], kind: str) -> str:
    number = pr.get("number")
    title = str(pr.get("title") or "").strip()
    url = str(pr.get("url") or "").strip()
    link = f"[#{number}]({url})" if url else f"#{number}"
    if kind == "dependabot":
        return f"- 🔲 [AUTO] Merge Dependabot {link} ({title})"
    return f"- 🔲 [AGENT] Merge release {link} ({title})"


def render_inner(prs: list[dict[str, Any]]) -> str:
    rows: list[str] = []
    for pr in prs:
        kind = classify_pr(pr)
        if kind:
            rows.append(format_row(pr, kind))
    autos = [r for r in rows if "[AUTO]" in r]
    agents = [r for r in rows if "[AGENT]" in r]
    ordered = autos + agents
    if not ordered:
        return EMPTY_NOTE
    return "\n".join(ordered)


def extract_inner(text: str) -> str | None:
    begin = text.find(BEGIN)
    end = text.find(END)
    if begin < 0 or end < 0 or end < begin:
        return None
    return text[begin + len(BEGIN) : end].strip("\n")


def replace_inner(text: str, inner: str) -> str:
    begin = text.find(BEGIN)
    end = text.find(END)
    if begin < 0 or end < 0 or end < begin:
        raise ValueError(f"missing {BEGIN} / {END} markers")
    start = begin + len(BEGIN)
    return text[:start] + "\n" + inner + "\n" + text[end:]


def relevant_prs(prs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [p for p in prs if classify_pr(p)]
