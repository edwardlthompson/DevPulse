#!/usr/bin/env python3
"""beforeShellExecution: deny destructive commands unless session approved. Fail-open."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent


def main() -> None:
    try:
        data = json.load(sys.stdin)
    except Exception:
        print(json.dumps({"permission": "allow"}))
        return

    command = (data.get("command") or "").strip()
    if not command:
        print(json.dumps({"permission": "allow"}))
        return

    bp = ROOT / "BUILD_PLAN.md"
    if bp.is_file() and "<!-- cursor-hooks: off -->" in bp.read_text(encoding="utf-8"):
        print(json.dumps({"permission": "allow"}))
        return

    deny_path = ROOT / ".cursor/hooks/shell-denylist.txt"
    patterns: list[str] = []
    if deny_path.is_file():
        for line in deny_path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                patterns.append(line.lower())

    approved: list[str] = []
    for name in (".cursor-session-state.json", ".cursor-session-state"):
        state_path = ROOT / name
        if state_path.is_file():
            try:
                state = json.loads(state_path.read_text(encoding="utf-8"))
                approved = state.get("destructive_ops_approved") or []
            except json.JSONDecodeError:
                pass
            break

    cmd_lower = command.lower()
    for pat in patterns:
        if pat in cmd_lower:
            for ok in approved:
                if ok.lower() in cmd_lower or (
                    ok.lower() in ("git push",) and "git push" in cmd_lower
                ):
                    print(json.dumps({"permission": "allow"}))
                    return
            print(
                json.dumps(
                    {
                        "permission": "deny",
                        "user_message": f"Blocked destructive command (hook): {command[:120]}",
                        "agent_message": (
                            "Use /push or /ship for git push approval; "
                            "set destructive_ops_approved in session state."
                        ),
                    }
                )
            )
            return

    print(json.dumps({"permission": "allow"}))


if __name__ == "__main__":
    main()
