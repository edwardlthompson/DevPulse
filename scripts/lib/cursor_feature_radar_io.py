"""Fetch and persist helpers for Cursor feature radar."""
from __future__ import annotations

import hashlib
import json
import re
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

LLMS_URL = "https://cursor.com/llms.txt"
URL_RE = re.compile(r"https://cursor\.com/docs/[^\s)]+")


def fetch_llms() -> tuple[str | None, str | None]:
    try:
        req = urllib.request.Request(
            LLMS_URL, headers={"User-Agent": "agent-project-bootstrap-radar/1.0"}
        )
        with urllib.request.urlopen(req, timeout=30) as resp:
            text = resp.read().decode("utf-8", errors="replace")
        digest = hashlib.sha256(text.encode("utf-8")).hexdigest()
        return text, digest
    except (urllib.error.URLError, TimeoutError, OSError) as exc:
        print(f"WARN: fetch failed: {exc}", file=sys.stderr)
        return None, None


def parse_urls(text: str) -> set[str]:
    return set(URL_RE.findall(text))


def load_registry(root: Path) -> dict:
    return json.loads((root / "docs/CURSOR_FEATURE_REGISTRY.json").read_text(encoding="utf-8"))


def save_registry(root: Path, data: dict) -> None:
    path = root / "docs/CURSOR_FEATURE_REGISTRY.json"
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8", newline="\n")


def score_new(url: str, tier: str) -> int:
    score = 5
    lower = url.lower()
    if any(k in lower for k in ("hook", "skill", "subagent", "mcp", "cli")):
        score += 2
    if "cloud" in lower or "bugbot" in lower or "automation" in lower:
        score += 1 if tier == "commercial" else -1
    if "design" in lower or "canvas" in lower:
        score += 1
    return max(0, min(10, score))


def read_backlog(root: Path) -> set[str]:
    path = root / "CURSOR_RADAR_BACKLOG.md"
    if not path.is_file():
        return set()
    return set(re.findall(r"https://cursor\.com/docs/[^\s)]+", path.read_text(encoding="utf-8")))


def write_suggestions(root: Path, scored: list[tuple[str, int]]) -> None:
    path = root / "CURSOR_RADAR_SUGGESTIONS.md"
    lines = [
        "# Suggested AGENT rows (gitignored)",
        "",
        "Paste into BUILD_PLAN after `/ideas` review. Not auto-applied.",
        "",
    ]
    ranked = [item for item in sorted(scored, key=lambda x: -x[1]) if item[1] >= 7]
    if not ranked:
        lines.append("No new URLs scored >= 7.")
        lines.append("")
        path.write_text("\n".join(lines), encoding="utf-8", newline="\n")
        return
    for url, score in ranked[:8]:
        lines.append(f"1. 🔲 [AGENT] Review Cursor doc ({score}): {url}")
    lines.append("")
    path.write_text("\n".join(lines), encoding="utf-8", newline="\n")


def write_build_plan_draft(root: Path, scored: list[tuple[str, int]]) -> None:
    """Gitignored BUILD_PLAN-shaped stub. Never writes BUILD_PLAN.md."""
    path = root / "CURSOR_RADAR_BUILD_PLAN_DRAFT.md"
    lines = [
        "### Radar draft (gitignored — paste after `/ideas` review)",
        "",
        "Do not merge this file. Do not auto-edit BUILD_PLAN.md.",
        "",
    ]
    ranked = [item for item in sorted(scored, key=lambda x: -x[1]) if item[1] >= 9]
    if not ranked:
        lines.append("_No URLs scored >= 9. Nothing to paste._")
        lines.append("")
    else:
        for index, (url, score) in enumerate(ranked[:6], start=1):
            slug = url.rstrip("/").split("/")[-1].replace("-", " ")
            lines.append(f"{index}. 🔲 [AGENT] Radar: {slug} ({score}) `{url}`")
        lines.append("")
    path.write_text("\n".join(lines), encoding="utf-8", newline="\n")


def append_backlog(root: Path, url: str, score: int) -> None:
    path = root / "CURSOR_RADAR_BACKLOG.md"
    if path.is_file() and url in path.read_text(encoding="utf-8"):
        return
    line = f"- [{score}] {url} ({datetime.now(timezone.utc).date().isoformat()})\n"
    with path.open("a", encoding="utf-8") as fh:
        if path.stat().st_size == 0:
            fh.write("# Cursor feature radar backlog (gitignored)\n\n")
        fh.write(line)
