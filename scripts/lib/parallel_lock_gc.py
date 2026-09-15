"""Delete stale .cursor/parallel-scope-lock.json after /scope finishes."""
from __future__ import annotations

import json
import subprocess
from datetime import datetime, timezone
from pathlib import Path

STALE_HOURS = 24


def _scope_prefixes(data: dict) -> list[str]:
    prefixes: list[str] = []
    for agent in data.get("agents") or []:
        if not isinstance(agent, dict):
            continue
        scope = agent.get("scope")
        if not isinstance(scope, str) or not scope.strip():
            continue
        cleaned = scope.strip().replace("\\", "/").rstrip("*").rstrip("/")
        if cleaned:
            prefixes.append(cleaned)
    return prefixes


def git_dirty_paths(root: Path) -> list[str]:
    try:
        proc = subprocess.run(
            ["git", "status", "--porcelain", "-u"],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=10,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return []
    if proc.returncode != 0:
        return []
    paths: list[str] = []
    for line in proc.stdout.splitlines():
        if len(line) < 4:
            continue
        rest = line[3:]
        if " -> " in rest:
            rest = rest.split(" -> ", 1)[1]
        paths.append(rest.strip().strip('"').replace("\\", "/"))
    return paths


def scopes_have_git_activity(root: Path, data: dict, dirty_paths: list[str] | None = None) -> bool:
    prefixes = _scope_prefixes(data)
    if not prefixes:
        return False
    dirty = dirty_paths if dirty_paths is not None else git_dirty_paths(root)
    for path in dirty:
        for prefix in prefixes:
            if path == prefix or path.startswith(prefix + "/"):
                return True
    return False


def _parse_created(raw: object) -> datetime | None:
    if not isinstance(raw, str) or not raw.strip():
        return None
    text = raw.strip().replace("Z", "+00:00")
    try:
        parsed = datetime.fromisoformat(text)
    except ValueError:
        return None
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=timezone.utc)
    return parsed


def gc_parallel_lock(
    root: Path,
    *,
    now: datetime | None = None,
    max_age_hours: int = STALE_HOURS,
    dirty_paths: list[str] | None = None,
) -> str:
    path = root / ".cursor" / "parallel-scope-lock.json"
    if not path.is_file():
        return "absent"
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        path.unlink(missing_ok=True)
        return "deleted-invalid"
    if not isinstance(data, dict):
        path.unlink(missing_ok=True)
        return "deleted-invalid"
    agents = data.get("agents") or []
    if not agents:
        path.unlink(missing_ok=True)
        return "deleted-empty"
    created = _parse_created(data.get("created_at"))
    if created is None:
        created = datetime.fromtimestamp(path.stat().st_mtime, tz=timezone.utc)
    stamp = now or datetime.now(timezone.utc)
    age_h = (stamp - created).total_seconds() / 3600
    if age_h > max_age_hours:
        if scopes_have_git_activity(root, data, dirty_paths=dirty_paths):
            return "kept-active"
        path.unlink(missing_ok=True)
        return "deleted-stale"
    return "kept"


def main() -> int:
    result = gc_parallel_lock(Path.cwd())
    print(f"parallel-lock gc: {result}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
