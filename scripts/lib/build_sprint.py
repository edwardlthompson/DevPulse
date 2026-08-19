"""Parse BUILD_PLAN for autonomous /build sprint execution."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from build_sprint_model import (
    is_template_repo,
    load_backlog_keys,
    load_progress,
    next_actionable_row,
    row_action,
)
from build_sprint_parse import parse_maintenance_rows, parse_sprint_blocks
from build_sprint_resolve import resolve_sprint
from weekly_health import is_recurring_weekly_auto, weekly_health_succeeded_this_week


def _maintainer_idle() -> dict:
    return {
        "lane": "maintainer",
        "sprint": "Template Maintainer",
        "sprint_agent_auto_complete": True,
        "sprint_complete": True,
        "open_agent_auto": 0,
        "open_human_adb": 0,
        "halt": False,
        "halt_reason": None,
        "next_row": None,
        "action": None,
        "chain_continue": False,
        "all_sprints_agent_auto_complete": True,
        "backlogged_human_adb": [],
    }


def _child_status(text: str, progress: dict, backlog_keys: set[str]) -> dict:
    for title, block_lines in parse_sprint_blocks(text):
        status = resolve_sprint(title, block_lines, progress, backlog_keys)
        if not status["sprint_agent_auto_complete"] or status.get("next_row"):
            status["lane"] = "child"
            status["chain_continue"] = status["sprint_agent_auto_complete"]
            status["all_sprints_agent_auto_complete"] = False
            return status
    idle = _maintainer_idle()
    idle["lane"] = "child"
    idle["sprint"] = None
    return idle


def build_status(root: Path, *, lane: str = "auto") -> dict:
    text = (root / "BUILD_PLAN.md").read_text(encoding="utf-8")
    progress = load_progress(root)
    backlog_keys = load_backlog_keys(root)
    template = is_template_repo(root)
    if lane == "auto" and template:
        lane = "maintainer"
    if lane in ("auto", "child"):
        child_status = _child_status(text, progress, backlog_keys)
        if not child_status.get("all_sprints_agent_auto_complete") or lane == "child":
            return child_status
    maint_auto, maint_human = parse_maintenance_rows(text)
    if template and weekly_health_succeeded_this_week(root):
        maint_auto = [row for row in maint_auto if not is_recurring_weekly_auto(row)]
    maint_aa_next = next_actionable_row(maint_auto, backlog_keys)
    maint_next = maint_aa_next or (
        next_actionable_row(maint_human, backlog_keys) if not maint_aa_next else None
    )
    if not maint_next:
        return _maintainer_idle() if (lane == "maintainer" or template) else _child_status(
            text, progress, backlog_keys
        )
    act = row_action(maint_next.owner) if maint_next.owner in ("HUMAN", "ADB") else "execute"
    return {
        "lane": "maintainer",
        "sprint": "Ongoing Maintenance",
        "sprint_agent_auto_complete": False,
        "sprint_complete": False,
        "open_agent_auto": len([r for r in maint_auto if r.owner in ("AGENT", "AUTO")]),
        "open_human_adb": len(maint_human),
        "halt": False,
        "halt_reason": None,
        "next_row": {
            "owner": maint_next.owner,
            "task": maint_next.task,
            "sprint": "Ongoing Maintenance",
            "phase": "maintenance",
            "action": act,
        },
        "action": act,
        "chain_continue": False,
        "all_sprints_agent_auto_complete": False,
        "backlogged_human_adb": [],
    }


def _configure_stdio() -> None:
    for stream in (sys.stdout, sys.stderr):
        reconf = getattr(stream, "reconfigure", None)
        if callable(reconf):
            try:
                reconf(encoding="utf-8", errors="replace")
            except (OSError, ValueError):
                pass


def main() -> int:
    import argparse

    _configure_stdio()
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--lane", default="auto", choices=["auto", "child", "maintainer"])
    args = parser.parse_args()
    root = Path(args.root).resolve()
    status = build_status(root, lane=args.lane)
    mode = "template" if is_template_repo(root) else "child"
    status["repo_mode"] = mode
    if args.json:
        print(json.dumps(status, indent=2, ensure_ascii=False))
        return 0
    print(f"Mode: {mode}")
    if status.get("all_sprints_agent_auto_complete") and not status.get("next_row"):
        if mode == "template":
            print("No open maintainer AGENT sprint. Child Sprint 0 is the playbook, not this repo.")
        else:
            print("All sprints: no open actionable rows")
    elif status.get("next_row"):
        nr = status["next_row"]
        print(f"NEXT [{nr['owner']}] {nr['task']} ({status['sprint']})")
    else:
        print(json.dumps(status, indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
