"""Classify open GitHub issues for /audit (fixes) and /ideas (features)."""
from __future__ import annotations

import json
import re
import subprocess
from typing import Any

SECURITY_HINTS = ("cve-", "security advisory", "private key leaked")
CRASH_FP = re.compile(r"\[crash\]\s+([a-f0-9]{12})", re.I)


def sanitize_board_title(title: str) -> str:
    cleaned = title.replace("|", "/").replace("\n", " ").replace("\r", " ")
    return cleaned[:80]


def _labels(issue: dict[str, Any]) -> set[str]:
    raw = issue.get("labels") or []
    names: set[str] = set()
    for item in raw:
        if isinstance(item, str):
            names.add(item.lower())
        elif isinstance(item, dict) and item.get("name"):
            names.add(str(item["name"]).lower())
    return names


def _security_suspect(issue: dict[str, Any]) -> bool:
    text = f"{issue.get('title') or ''}\n{issue.get('body') or ''}".lower()
    return any(hint in text for hint in SECURITY_HINTS)


def _number(issue: dict[str, Any]) -> int | None:
    num = issue.get("number")
    return int(num) if isinstance(num, int) else None


def already_tracked(number: int | None, board_text: str) -> bool:
    if number is None:
        return False
    return f"#{number}" in board_text


def classify_issues(
    issues: list[dict[str, Any]],
    discussions: list[dict[str, Any]] | None = None,
    board_text: str = "",
    truncated: bool = False,
) -> dict[str, Any]:
    fixes: list[dict[str, Any]] = []
    features: list[dict[str, Any]] = []
    blocked: list[dict[str, Any]] = []
    security: list[dict[str, Any]] = []
    for issue in issues:
        num = _number(issue)
        if already_tracked(num, board_text):
            continue
        labels = _labels(issue)
        title = str(issue.get("title") or "")
        fp_match = CRASH_FP.search(title)
        row = {
            "number": num,
            "title": sanitize_board_title(title),
            "labels": sorted(labels),
            "url": issue.get("url") or issue.get("html_url") or "",
            "fingerprint": fp_match.group(1) if fp_match else "",
            "created_at": issue.get("created_at") or "",
        }
        if _security_suspect(issue):
            security.append(row)
            continue
        if "crash" in labels or "bug" in labels:
            if "needs-repro" in labels:
                blocked.append(row)
            else:
                fixes.append(row)
        elif "enhancement" in labels:
            features.append(row)
    for disc in discussions or []:
        num = _number(disc)
        if already_tracked(num, board_text):
            continue
        features.append(
            {
                "number": num,
                "title": sanitize_board_title(str(disc.get("title") or "")),
                "labels": ["discussion"],
                "url": disc.get("url") or "",
                "fingerprint": "",
                "created_at": disc.get("created_at") or "",
            }
        )
    fixes.sort(key=lambda r: (0 if "crash" in r["labels"] else 1, r.get("created_at") or ""))
    return {
        "fixes": fixes,
        "features": features,
        "blocked": blocked,
        "security_suspect": security,
        "truncated": truncated,
    }


def fetch_issues(limit: int = 50) -> tuple[list[dict[str, Any]], bool]:
    try:
        proc = subprocess.run(
            [
                "gh",
                "issue",
                "list",
                "--state",
                "open",
                "--limit",
                str(limit),
                "--json",
                "number,title,labels,url,createdAt,body",
            ],
            capture_output=True,
            text=True,
            timeout=10,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return [], False
    if proc.returncode != 0:
        return [], False
    try:
        items = json.loads(proc.stdout or "[]")
    except json.JSONDecodeError:
        return [], False
    for item in items:
        if "createdAt" in item and "created_at" not in item:
            item["created_at"] = item["createdAt"]
    return items, len(items) >= limit
