"""Init --profile core|full: slim vs full template emission."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

PROFILES = frozenset({"core", "full"})
CORE_FILES = frozenset(
    {
        "AGENTS.md",
        "LICENSE",
        "SECURITY.md",
        "CONTRIBUTING.md",
        "CODE_OF_CONDUCT.md",
        ".gitignore",
        ".gitattributes",
    }
)
CORE_DIRS = frozenset({".github", ".agent", ".git"})
SLIM_MEMORY = """# Agent memory (core profile)

Slim memory for `--profile core`. Expand with `--profile full` or copy from the template.

## Persistent Context

- Stack: see `bootstrap.config.json`
- Keep milestones short; prefer deleting to adding.
"""


def normalize_profile(raw: str | None) -> str:
    value = (raw or "core").strip().lower()
    return value if value in PROFILES else "core"


def write_slim_memory(root: Path) -> Path:
    dest = root / ".agent" / "AGENT_MEMORY.md"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(SLIM_MEMORY, encoding="utf-8")
    return dest


def apply_core_profile(root: Path, *, dry_run: bool = False) -> list[str]:
    """Keep only core surface files/dirs; remove the rest (except .git)."""
    actions: list[str] = []
    write_slim_memory(root) if not dry_run else None
    actions.append("core: ensure .agent/AGENT_MEMORY.md")
    for child in sorted(root.iterdir(), key=lambda p: p.name):
        name = child.name
        if name in CORE_DIRS or name in CORE_FILES:
            continue
        if name == "bootstrap.config.json":
            continue
        actions.append(f"core: remove {name}")
        if dry_run:
            continue
        if child.is_dir():
            shutil.rmtree(child)
        else:
            child.unlink()
    # Drop unexpected files under .agent except AGENT_MEMORY.md
    agent = root / ".agent"
    if agent.is_dir():
        for child in list(agent.rglob("*")):
            if child.is_dir():
                continue
            rel = child.relative_to(root).as_posix()
            if rel != ".agent/AGENT_MEMORY.md":
                actions.append(f"core: remove {rel}")
                if not dry_run:
                    child.unlink()
    return actions


def relocate_memory_logs(root: Path) -> list[str]:
    """Full profile: ensure memory/log live under .agent/."""
    actions: list[str] = []
    mapping = {
        "AGENT_MEMORY.md": ".agent/AGENT_MEMORY.md",
        "DECISION_LOG.md": ".agent/DECISION_LOG.md",
    }
    for src_name, dest_name in mapping.items():
        src = root / src_name
        dest = root / dest_name
        if src.is_file() and not dest.is_file():
            dest.parent.mkdir(parents=True, exist_ok=True)
            src.replace(dest)
            actions.append(f"full: move {src_name} → {dest_name}")
        elif dest.is_file():
            actions.append(f"full: keep {dest_name}")
    return actions


def stamp_profile(root: Path, profile: str) -> None:
    path = root / "bootstrap.config.json"
    data: dict = {}
    if path.is_file():
        try:
            loaded = json.loads(path.read_text(encoding="utf-8"))
            if isinstance(loaded, dict):
                data = loaded
        except json.JSONDecodeError:
            data = {}
    data["profile"] = normalize_profile(profile)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def check_profile(root: Path) -> tuple[int, str]:
    path = root / "bootstrap.config.json"
    if not path.is_file():
        return 0, "OK: no bootstrap.config.json (profile gate skipped)"
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return 1, "bootstrap.config.json is not valid JSON"
    if not isinstance(data, dict):
        return 1, "bootstrap.config.json must be an object"
    profile = normalize_profile(str(data.get("profile") or "full"))
    if profile == "full":
        return 0, "OK: profile=full"
    # core: examples/ and modules/ must be absent
    for rel in ("examples", "modules", "docs", "scripts"):
        if (root / rel).exists():
            return 1, (
                f"profile=core but {rel}/ still present. "
                "Re-run init with --profile core or remove scaffolding."
            )
    if not (root / ".agent" / "AGENT_MEMORY.md").is_file():
        return 1, "profile=core missing .agent/AGENT_MEMORY.md"
    return 0, "OK: profile=core surface"
