"""Working-tree notes for project-health (dirty Unreleased / unpushed HEAD)."""
from __future__ import annotations

import re
import subprocess
from pathlib import Path


def _git(root: Path, *args: str) -> str:
    try:
        proc = subprocess.run(
            ["git", *args],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=10,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return ""
    return proc.stdout if proc.returncode == 0 else ""


def unreleased_has_entries(root: Path) -> bool:
    path = root / "CHANGELOG.md"
    if not path.is_file():
        return False
    text = path.read_text(encoding="utf-8")
    match = re.search(r"## \[Unreleased\](.*?)(\n## |\Z)", text, re.S)
    if not match:
        return False
    body = match.group(1)
    return bool(re.search(r"^\s*[\*\-]", body, re.M))


def collect_health_notes(root: Path) -> list[str]:
    notes: list[str] = []
    porcelain = _git(root, "status", "--porcelain")
    if porcelain.strip():
        notes.append("Working tree is dirty (uncommitted files).")
    ahead = _git(root, "rev-list", "--count", "@{upstream}..HEAD").strip()
    if ahead.isdigit() and int(ahead) > 0:
        notes.append(f"HEAD is {ahead} commit(s) ahead of upstream (unpushed).")
    if unreleased_has_entries(root):
        notes.append("CHANGELOG [Unreleased] has entries — commit/push before /ship.")
    return notes
