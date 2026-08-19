"""Sanitize the environment passed to agent-run child processes."""
from __future__ import annotations

import os
from pathlib import Path


def windows_tool_dirs() -> list[Path]:
    if os.name != "nt":
        return []
    return [
        Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "GitHub CLI",
        Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "nodejs",
        Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Git" / "bin",
        Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "GitHub CLI",
    ]


def child_env(base: dict[str, str] | None = None) -> dict[str, str]:
    env = dict(base if base is not None else os.environ)
    env.pop("PYTHONPATH", None)
    extras = [p for p in windows_tool_dirs() if p.is_dir()]
    if extras:
        prefix = os.pathsep.join(str(p) for p in extras)
        env["PATH"] = prefix + os.pathsep + env.get("PATH", "")
    return env
