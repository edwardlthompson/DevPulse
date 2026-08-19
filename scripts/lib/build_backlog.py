"""Manage HUMAN_BACKLOG.md for failed /build automation attempts."""
from __future__ import annotations

import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

HEADER = """# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

| Deferred | Sprint | Owner | Task | Reason |
|----------|--------|-------|------|--------|
"""


def backlog_path(root: Path) -> Path:
    return root / "HUMAN_BACKLOG.md"


def read_rows(text: str) -> list[tuple[str, str, str, str, str]]:
    rows: list[tuple[str, str, str, str, str]] = []
    for line in text.splitlines():
        if not line.startswith("|") or "---" in line or "Deferred" in line:
            continue
        parts = [p.strip() for p in line.strip("|").split("|")]
        if len(parts) >= 5:
            rows.append(tuple(parts[:5]))  # type: ignore[arg-type]
    return rows


def add_item(root: Path, owner: str, task: str, sprint: str, reason: str) -> bool:
    path = backlog_path(root)
    if not path.exists():
        path.write_text(HEADER, encoding="utf-8")
    text = path.read_text(encoding="utf-8")
    key = f"{sprint}|{task}"
    for row in read_rows(text):
        if f"{row[1]}|{row[3]}" == key:
            return False
    ts = datetime.now(timezone.utc).replace(microsecond=0).date().isoformat()
    task_cell = task.replace("|", "\\|")
    reason_cell = reason.replace("|", "\\|")[:200]
    line = f"| {ts} | {sprint} | {owner} | {task_cell} | {reason_cell} |\n"
    path.write_text(text.rstrip() + "\n" + line, encoding="utf-8")
    return True


def list_items(root: Path) -> list[dict]:
    path = backlog_path(root)
    if not path.exists():
        return []
    return [
        {"deferred": r[0], "sprint": r[1], "owner": r[2], "task": r[3], "reason": r[4]}
        for r in read_rows(path.read_text(encoding="utf-8"))
    ]


def main() -> int:
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    sub = parser.add_subparsers(dest="command", required=True)

    add_p = sub.add_parser("add")
    add_p.add_argument("--owner", required=True)
    add_p.add_argument("--task", required=True)
    add_p.add_argument("--sprint", default="")
    add_p.add_argument("--reason", default="automation failed")

    sub.add_parser("list", help="List backlog items")

    args = parser.parse_args()
    root = Path(args.root).resolve()

    if args.command == "add":
        added = add_item(root, args.owner, args.task, args.sprint, args.reason)
        print("added" if added else "duplicate")
        return 0
    if args.command == "list":
        print(json.dumps(list_items(root), indent=2))
        return 0
    return 1


if __name__ == "__main__":
    sys.exit(main())
