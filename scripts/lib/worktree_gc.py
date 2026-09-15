"""GC stale `.cursor/worktrees/` dirs from setup-agent-worktrees (not Cursor native)."""
from __future__ import annotations

import json
import shutil
import subprocess
from datetime import datetime, timezone
from pathlib import Path

STALE_HOURS = 24
REL_DIR = Path(".cursor") / "worktrees"


def _git(root: Path, *args: str) -> tuple[int, str]:
    try:
        proc = subprocess.run(
            ["git", *args],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=30,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return 1, ""
    return proc.returncode, (proc.stdout or "") + (proc.stderr or "")


def lock_protected_paths(root: Path) -> set[Path]:
    lock = root / ".cursor" / "parallel-scope-lock.json"
    if not lock.is_file():
        return set()
    try:
        data = json.loads(lock.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return set()
    if not isinstance(data, dict):
        return set()
    protected: set[Path] = set()
    for agent in data.get("agents") or []:
        if not isinstance(agent, dict):
            continue
        branch = str(agent.get("branch") or "")
        slug = branch.replace("feature/agent-", "") or str(agent.get("id") or "").lower()
        if slug:
            protected.add((root / REL_DIR / slug).resolve())
    return protected


def parse_worktree_paths(porcelain: str) -> set[Path]:
    paths: set[Path] = set()
    for line in porcelain.splitlines():
        if line.startswith("worktree "):
            paths.add(Path(line[len("worktree ") :]).resolve())
    return paths


def plan_actions(
    *,
    children: list[tuple[Path, datetime]],
    registered: set[Path],
    protected: set[Path],
    cwd: Path,
    now: datetime,
    max_age_hours: int = STALE_HOURS,
) -> list[tuple[str, Path]]:
    cwd_r = cwd.resolve()
    planned: list[tuple[str, Path]] = []
    for path, mtime in children:
        resolved = path.resolve()
        if resolved == cwd_r or resolved in protected:
            planned.append(("keep", resolved))
            continue
        age_h = (now - mtime).total_seconds() / 3600
        if age_h <= max_age_hours:
            planned.append(("keep", resolved))
            continue
        if resolved in registered:
            planned.append(("remove-stale", resolved))
        else:
            planned.append(("remove-orphan", resolved))
    return planned


def scan_children(root: Path) -> list[tuple[Path, datetime]]:
    base = root / REL_DIR
    if not base.is_dir():
        return []
    out: list[tuple[Path, datetime]] = []
    for child in sorted(base.iterdir()):
        if child.is_dir():
            mtime = datetime.fromtimestamp(child.stat().st_mtime, tz=timezone.utc)
            out.append((child, mtime))
    return out


def gc_worktrees(root: Path, *, apply: bool = False, now: datetime | None = None) -> str:
    stamp = now or datetime.now(timezone.utc)
    _git(root, "worktree", "prune")
    code, porcelain = _git(root, "worktree", "list", "--porcelain")
    registered = parse_worktree_paths(porcelain) if code == 0 else set()
    planned = plan_actions(
        children=scan_children(root),
        registered=registered,
        protected=lock_protected_paths(root),
        cwd=Path.cwd(),
        now=stamp,
    )
    removed = 0
    for action, path in planned:
        if action not in {"remove-stale", "remove-orphan"}:
            continue
        if not apply:
            print(f"worktree gc dry-run: {action} {path}")
            continue
        if action == "remove-stale":
            _git(root, "worktree", "remove", "--force", str(path))
        if path.is_dir():
            shutil.rmtree(path, ignore_errors=True)
        removed += 1
    if apply:
        _git(root, "clean", "-fdX", "--", str(REL_DIR.as_posix()))
        _git(root, "worktree", "prune")
    summary = f"planned={len(planned)} removed={removed} apply={apply}"
    print(f"worktree gc: {summary}")
    return summary


def main(argv: list[str] | None = None) -> int:
    apply = "--apply" in (argv or [])
    gc_worktrees(Path.cwd(), apply=apply)
    return 0


if __name__ == "__main__":
    import sys

    raise SystemExit(main(sys.argv[1:]))
