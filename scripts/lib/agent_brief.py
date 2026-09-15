"""Parse AGENT.md markers and render BUILD_PLAN Product (do not drift) inner."""
from __future__ import annotations

import re
from typing import Any

ONE_BEGIN = "<!-- agent-brief:one-liner -->"
ONE_END = "<!-- /agent-brief:one-liner -->"
KEY_BEGIN = "<!-- agent-brief:keywords -->"
KEY_END = "<!-- /agent-brief:keywords -->"
BEGIN = "<!-- product-brief-sync:begin -->"
END = "<!-- product-brief-sync:end -->"
EMPTY_NOTE = (
    "_Template maintainer: no product AGENT.md. "
    "Children write AGENT.md before init._"
)


def _between(text: str, start: str, end: str) -> str:
    a, b = text.find(start), text.find(end)
    if a < 0 or b < 0 or b < a:
        return ""
    return text[a + len(start) : b].strip()


def extract_inner(text: str) -> str | None:
    a, b = text.find(BEGIN), text.find(END)
    if a < 0 or b < 0 or b < a:
        return None
    return text[a + len(BEGIN) : b].strip("\n")


def replace_inner(text: str, inner: str) -> str:
    a, b = text.find(BEGIN), text.find(END)
    if a < 0 or b < 0 or b < a:
        raise ValueError("missing product-brief-sync markers")
    return text[: a + len(BEGIN)] + "\n" + inner + "\n" + text[b:]


def parse_keywords(blob: str) -> list[str]:
    parts = re.split(r"[,;\n]+", blob)
    return [p.strip().lower() for p in parts if p.strip()]


def parse_brief(text: str) -> dict[str, Any]:
    one = _between(text, ONE_BEGIN, ONE_END)
    keys = parse_keywords(_between(text, KEY_BEGIN, KEY_END))
    rules = ""
    mile = ""
    for raw in text.split("## "):
        title, _, body = raw.partition("\n")
        name = title.strip().lower()
        chunk = body.strip()
        if name.startswith("rules"):
            rules = chunk
        elif name.startswith("first milestone"):
            mile = chunk.split("\n", 1)[0].strip(" -")
    return {"one_liner": one, "keywords": keys, "rules": rules, "milestone": mile}


def render_inner(brief: dict[str, Any] | None) -> str:
    if not brief or not str(brief.get("one_liner") or "").strip():
        return EMPTY_NOTE
    one = str(brief["one_liner"]).strip()
    keys = [str(k) for k in (brief.get("keywords") or []) if str(k).strip()]
    rules = str(brief.get("rules") or "").strip()
    mile = str(brief.get("milestone") or "").strip()
    lines = [
        "> Read `AGENT.md` before any sprint row.",
        "",
        f"**One-liner:** {one}",
    ]
    if keys:
        lines.append(f"**Do not drift:** {', '.join(keys)}")
    if rules:
        lines.extend(["", "**Rules:**", rules])
    if mile:
        lines.extend(["", f"**First milestone:** {mile}"])
    return "\n".join(lines)
