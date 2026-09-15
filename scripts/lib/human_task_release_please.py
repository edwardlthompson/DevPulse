"""Merge Release Please PRs when branch policy allows."""
from __future__ import annotations

import json
import subprocess
from pathlib import Path

from human_task_core import AttemptResult


def automate_release_please_merge(root: Path, _cfg: dict) -> AttemptResult:
    """Merge open Release Please PRs when GitHub reports MERGEABLE (no local push)."""
    proc = subprocess.run(
        [
            "gh",
            "pr",
            "list",
            "--state",
            "open",
            "--json",
            "number,title,mergeable,author,url",
            "--limit",
            "30",
        ],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        return AttemptResult(1, "release-please", (proc.stderr or proc.stdout or "")[-400:], True)
    try:
        data = json.loads(proc.stdout or "[]")
    except json.JSONDecodeError:
        return AttemptResult(1, "release-please", "invalid pr list JSON", True)
    if not isinstance(data, list):
        return AttemptResult(1, "release-please", "unexpected pr list", True)

    candidates = [
        pr
        for pr in data
        if "chore(main): release" in str(pr.get("title") or "").lower()
        or "release-please" in str(pr.get("title") or "").lower()
    ]
    if not candidates:
        return AttemptResult(0, "release-please", "No open Release Please PRs", False)

    merged: list[str] = []
    blocked: list[str] = []
    for pr in candidates:
        num = pr.get("number")
        if pr.get("mergeable") != "MERGEABLE":
            blocked.append(f"#{num}:{pr.get('mergeable')}")
            continue
        m = subprocess.run(
            ["gh", "pr", "merge", str(num), "--merge", "--delete-branch"],
            cwd=root,
            capture_output=True,
            text=True,
            check=False,
        )
        if m.returncode == 0:
            merged.append(f"#{num}")
        else:
            blocked.append(f"#{num}:{(m.stderr or m.stdout or str(m.returncode))[-200:]}")

    if merged and not blocked:
        return AttemptResult(0, "release-please", f"Merged Release Please {', '.join(merged)}", False)
    if merged:
        return AttemptResult(
            1,
            "release-please",
            f"merged {','.join(merged)}; blocked {';'.join(blocked)}"[:400],
            True,
        )
    return AttemptResult(
        1,
        "release-please",
        f"Release Please not merged: {'; '.join(blocked)}"[:400],
        True,
    )
