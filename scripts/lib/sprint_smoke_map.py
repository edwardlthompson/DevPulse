"""Map BUILD_PLAN task text to sprint-smoke probe names."""
from __future__ import annotations

import re
from typing import Iterable

from sprint_smoke_parse import SmokeItem

WORDS: dict[str, tuple[str, ...]] = {
    "web": (
        "web",
        "chrome",
        "lighthouse",
        "snapshot",
        "locale",
        "i18n",
        "vite",
        "playwright",
        "tokeniz",
        "empty state",
        "in-panel",
    ),
    "android": (
        "android",
        "compose",
        "talkback",
        "r8",
        "fdroid",
        "apk",
        "gradle",
        "unifiedpush",
        "rtl",
        "instrumented",
        "nav-stack",
        "runtime-budget",
    ),
    "node": ("node", "openapi spec", "hono"),
    "python": ("python", "mypy", "--feedback"),
    "rust": ("rust", "cargo.toml"),
    "go": (" go", "golang", "/module"),
    "lightroom": ("lightroom", "lua", "lr*"),
    "blender": ("blender", "cycles", "optix", "icon-factory"),
    "docs": (
        "docs/",
        "module.md",
        "catalog",
        "adr",
        "tour",
        "coach",
        "skill",
        "runbook",
        "winget",
        "dependabot",
        "scorecard",
        "semgrep",
        "nix",
        "radar",
        "ci-gap",
        "grok",
        "cursor",
        "automations",
        "canvas",
        "template-gap",
        "fastlane",
        "antifeatures",
        "signing",
        "glitchtip",
        "sanitizer",
        "automerge",
    ),
}

_BACKTICK = re.compile(r"`([^`]+)`")


def infer_probes(task: str) -> list[str]:
    lower = f" {task.lower()} "
    found: list[str] = []
    for name, needles in WORDS.items():
        if any(n in lower for n in needles):
            found.append(name)
    if "openapi" in lower and "python" in lower and "node" not in found:
        found.append("python")
    if "openapi" in lower and "node" in lower and "node" not in found:
        found.append("node")
    if not found:
        found.append("docs")
    # Preserve order, unique
    return list(dict.fromkeys(found))


def probes_for_items(items: Iterable[SmokeItem]) -> dict[str, list[str]]:
    return {f"{item.number}:{item.task}": infer_probes(item.task) for item in items}


def _is_slash_command(text: str) -> bool:
    token = text.split()[0] if text else ""
    return token.startswith("/") and token.count("/") == 1 and "." not in token


def backtick_paths(task: str) -> list[str]:
    paths: list[str] = []
    for raw in _BACKTICK.findall(task):
        text = raw.strip()
        token = text.split()[0] if text else ""
        if _is_slash_command(token):
            continue
        # Host paths are not repo docs probes (e.g. `~/.local/bin`, absolute SDK trees).
        if token.startswith("~") or token.startswith("/") or re.match(r"^[A-Za-z]:[\\/]", token):
            continue
        # "no `foo`" / "without `foo`" means absence is the requirement.
        if re.search(rf"\b(?:no|without|forbid(?:den)?)\s+`{re.escape(raw)}`", task, re.I):
            continue
        if "/" in text or token.endswith((".md", ".json", ".yml", ".yaml", ".html")):
            paths.append(token)
    return paths
