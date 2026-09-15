"""Fetch open GitHub PRs via gh for BUILD_PLAN sync."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
from pathlib import Path
from typing import Any


def resolve_gh() -> str | None:
    which = shutil.which("gh")
    if which:
        return which
    candidates: list[Path] = []
    if os.name == "nt":
        for base in (
            os.environ.get("ProgramFiles", r"C:\Program Files"),
            os.environ.get("LocalAppData", ""),
        ):
            if base:
                candidates.append(Path(base) / "GitHub CLI" / "gh.exe")
    for path in (
        Path("/mnt/c/Program Files/GitHub CLI/gh.exe"),
        Path("/c/Program Files/GitHub CLI/gh.exe"),
    ):
        candidates.append(path)
    for candidate in candidates:
        if candidate.is_file():
            return str(candidate)
    return None


def fetch_open_prs(root: Path, *, timeout: float = 15.0) -> list[dict[str, Any]]:
    gh = resolve_gh()
    if not gh:
        raise RuntimeError("gh CLI not found on PATH")
    proc = subprocess.run(
        [
            gh,
            "pr",
            "list",
            "--state",
            "open",
            "--limit",
            "100",
            "--json",
            "number,title,url,author,labels,headRefName",
        ],
        cwd=root,
        capture_output=True,
        text=True,
        timeout=timeout,
        check=False,
    )
    if proc.returncode != 0:
        err = (proc.stderr or proc.stdout or "").strip()
        raise RuntimeError(err or f"gh pr list failed ({proc.returncode})")
    data = json.loads(proc.stdout or "[]")
    if not isinstance(data, list):
        raise RuntimeError("gh pr list returned non-list JSON")
    return [p for p in data if isinstance(p, dict)]
