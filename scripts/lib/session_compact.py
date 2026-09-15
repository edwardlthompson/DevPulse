"""Merge Unreleased + HUMAN/ADB rows into .cursor-session-state.json."""
from __future__ import annotations

import json
import re
from datetime import datetime, timezone
from pathlib import Path

from health_ci import ci_red_one_liner
from health_notes import unreleased_has_entries
from parallel_lock_gc import gc_parallel_lock


def open_human_adb_rows(root: Path, limit: int = 20) -> list[str]:
    path = root / "BUILD_PLAN.md"
    if not path.is_file():
        return []
    rows: list[str] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if "🔲" not in line:
            continue
        if "[HUMAN]" not in line and "[ADB]" not in line:
            continue
        rows.append(line.strip()[:160])
        if len(rows) >= limit:
            break
    return rows


def unreleased_excerpt(root: Path, limit: int = 8) -> list[str]:
    path = root / "CHANGELOG.md"
    if not path.is_file():
        return []
    text = path.read_text(encoding="utf-8")
    match = re.search(r"## \[Unreleased\](.*?)(\n## |\Z)", text, re.S)
    if not match:
        return []
    items: list[str] = []
    for line in match.group(1).splitlines():
        stripped = line.strip()
        if stripped.startswith(("*", "-")):
            items.append(stripped[:200])
        if len(items) >= limit:
            break
    return items


def merge_compact(root: Path, extra: dict[str, object] | None = None) -> dict[str, object]:
    state_path = root / ".cursor-session-state.json"
    data: dict[str, object] = {}
    if state_path.is_file():
        try:
            loaded = json.loads(state_path.read_text(encoding="utf-8"))
            if isinstance(loaded, dict):
                data = loaded
        except json.JSONDecodeError:
            data = {}
    data["saved_at"] = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    data["unreleased_has_entries"] = unreleased_has_entries(root)
    data["unreleased_excerpt"] = unreleased_excerpt(root)
    data["open_human_adb_rows"] = open_human_adb_rows(root)
    data["last_ci_conclusion"] = ci_red_one_liner(root)
    if extra:
        data.update(extra)
    state_path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    return data


def main() -> int:
    root = Path.cwd()
    data = merge_compact(root)
    gc = gc_parallel_lock(root)
    n_u = len(data.get("unreleased_excerpt") or [])
    n_h = len(data.get("open_human_adb_rows") or [])
    ci = data.get("last_ci_conclusion") or ""
    print(
        f"session compact: unreleased_items={n_u} human_adb_rows={n_h} "
        f"parallel_lock={gc} last_ci={ci}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
