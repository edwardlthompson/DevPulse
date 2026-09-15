"""Smoke tests for .cursor/hooks.json shell guard."""
from __future__ import annotations

import json
import subprocess
from pathlib import Path


def _hooks_disabled(root: Path) -> bool:
    bp = root / "BUILD_PLAN.md"
    return bp.is_file() and "<!-- cursor-hooks: off -->" in bp.read_text(encoding="utf-8")


def run_guard(root: Path, command: str) -> dict:
    payload = json.dumps({"command": command})
    guard = root / ".cursor/hooks/before_shell_guard.py"
    proc = subprocess.run(
        ["python3", str(guard)],
        input=payload,
        capture_output=True,
        text=True,
        cwd=root.as_posix(),
        check=False,
    )
    out = proc.stdout.strip() or proc.stderr.strip()
    if not out:
        return {"permission": "allow"}
    try:
        return json.loads(out)
    except json.JSONDecodeError:
        return {"permission": "allow"}


def smoke(root: Path) -> list[str]:
    errors: list[str] = []
    if _hooks_disabled(root):
        return errors
    state = root / ".cursor-session-state.json"
    backup = state.read_text(encoding="utf-8") if state.is_file() else None
    try:
        state.write_text(
            json.dumps({"version": 1, "destructive_ops_approved": []}, indent=2)
            + "\n",
            encoding="utf-8",
        )
        if run_guard(root, "git status").get("permission") == "deny":
            errors.append("smoke: git status should be allowed")
        if run_guard(root, "git push origin main").get("permission") != "deny":
            errors.append("smoke: git push should be denied without session approval")
        state.write_text(
            json.dumps(
                {"version": 1, "destructive_ops_approved": ["git push"]}, indent=2
            )
            + "\n",
            encoding="utf-8",
        )
        if run_guard(root, "git push origin main").get("permission") != "allow":
            errors.append(
                "smoke: git push should be allowed with destructive_ops_approved"
            )
        if run_guard(root, "git push --force origin main").get("permission") != "deny":
            errors.append(
                "smoke: git push --force must stay denied when only git push is approved"
            )
    finally:
        if backup is None:
            if state.is_file():
                state.unlink()
        else:
            state.write_text(backup, encoding="utf-8")
    return errors
