"""Cloud → PC handoff digest: fetch, sync open PRs, print next AGENT row."""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path
from typing import Any

from resume_digest import cursor_prs, format_digest
from sync_open_prs_build_plan import extract_inner, fetch_open_prs, relevant_prs, sync_file

ROOT_DEFAULT = Path(__file__).resolve().parents[2]


def _run(root: Path, argv: list[str], *, timeout: float = 10.0) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        argv, cwd=root, capture_output=True, text=True, timeout=timeout, check=False
    )


def cloud_pr_changed_files(root: Path, prs: list[dict[str, Any]]) -> list[str]:
    """Best-effort list of paths changed on open cursor/* PRs."""
    files: list[str] = []
    for pr in prs:
        number = pr.get("number")
        if number is None:
            continue
        try:
            proc = _run(
                root,
                ["gh", "pr", "view", str(number), "--json", "files"],
                timeout=20.0,
            )
        except (OSError, subprocess.TimeoutExpired):
            continue
        if proc.returncode != 0:
            continue
        try:
            data = json.loads(proc.stdout or "{}")
        except json.JSONDecodeError:
            continue
        for item in data.get("files") or []:
            path = str((item or {}).get("path") or "").strip()
            if path:
                files.append(path)
    return files


def git_fetch(root: Path) -> str:
    try:
        proc = _run(root, ["git", "fetch", "origin"], timeout=15.0)
    except (OSError, subprocess.TimeoutExpired) as exc:
        return f"git fetch failed: {exc}"
    if proc.returncode != 0:
        return f"git fetch failed: {(proc.stderr or proc.stdout or '').strip()}"
    return "ok"


def branch_status(root: Path, branch: str = "main") -> list[str]:
    notes: list[str] = []
    try:
        dirty = _run(root, ["git", "status", "--porcelain"])
        if dirty.returncode == 0 and dirty.stdout.strip():
            notes.append("Working tree is dirty (uncommitted files) - will not reset or pull.")
        behind = _run(root, ["git", "rev-list", "--count", f"HEAD..origin/{branch}"])
        if behind.returncode == 0 and behind.stdout.strip().isdigit():
            n = int(behind.stdout.strip())
            if n > 0:
                tip = (
                    "Run `git pull` on a clean tree."
                    if not any("dirty" in x for x in notes)
                    else "Commit or stash, then `git pull`."
                )
                notes.append(f"Local HEAD is {n} commit(s) behind origin/{branch}. {tip}")
        ahead = _run(root, ["git", "rev-list", "--count", f"origin/{branch}..HEAD"])
        if ahead.returncode == 0 and ahead.stdout.strip().isdigit():
            n = int(ahead.stdout.strip())
            if n > 0:
                notes.append(f"Local HEAD is {n} commit(s) ahead of origin/{branch} (unpushed).")
    except (OSError, subprocess.TimeoutExpired) as exc:
        notes.append(f"git status failed: {exc}")
    return notes


def resume(root: Path, *, apply_sync: bool = True) -> tuple[int, str]:
    fetch_note = git_fetch(root)
    branch_notes = branch_status(root)
    plan = root / "BUILD_PLAN.md"
    gh_error: str | None = None
    prs: list[dict[str, Any]] = []
    try:
        prs = fetch_open_prs(root)
    except (OSError, subprocess.TimeoutExpired, RuntimeError, json.JSONDecodeError) as exc:
        gh_error = str(exc)

    sync_inner: str | None = None
    if plan.is_file() and extract_inner(plan.read_text(encoding="utf-8")) is not None:
        if apply_sync and gh_error is None:
            sync_file(plan, prs, apply=True, check=False, root=root)
        sync_inner = extract_inner(plan.read_text(encoding="utf-8"))

    cloud = cursor_prs(prs) if not gh_error else []
    hits: list[str] = []
    if plan.is_file() and cloud and not gh_error:
        try:
            from agent_venue import local_scopes_hit_by_files

            changed = cloud_pr_changed_files(root, cloud)
            hits = local_scopes_hit_by_files(plan.read_text(encoding="utf-8"), changed)
        except Exception:
            hits = []

    digest = format_digest(
        root,
        sync_inner=sync_inner,
        synced_prs=relevant_prs(prs) if not gh_error else [],
        cloud_prs=cloud,
        gh_error=gh_error,
        fetch_note=fetch_note,
        branch_notes=branch_notes,
        local_scope_hits=hits,
    )
    return 0, digest


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=str(ROOT_DEFAULT))
    parser.add_argument("--no-sync", action="store_true")
    args = parser.parse_args(argv)
    code, digest = resume(Path(args.root).resolve(), apply_sync=not args.no_sync)
    out = sys.stdout
    try:
        out.reconfigure(encoding="utf-8", errors="replace")  # type: ignore[attr-defined]
    except Exception:
        pass
    out.write(digest)
    return code


if __name__ == "__main__":
    sys.exit(main())
