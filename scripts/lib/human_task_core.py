"""Shared types and helpers for HUMAN/ADB row automation."""
from __future__ import annotations

import json
import os
import subprocess
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path


@dataclass
class AttemptResult:
    exit_code: int
    method: str
    reason: str
    backlog: bool

    def to_dict(self) -> dict:
        return {
            "exit": self.exit_code,
            "method": self.method,
            "reason": self.reason,
            "backlog": self.backlog,
        }


def load_json(path: Path) -> dict:
    if not path.exists():
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}


def resolve_config(root: Path) -> dict[str, str]:
    cfg: dict[str, str] = {}
    for key, env in (
        ("stack", "BUILD_STACK"),
        ("project_name", "BUILD_PROJECT_NAME"),
        ("purpose", "BUILD_PURPOSE"),
        ("release_repo", "GITHUB_REPO"),
    ):
        if os.environ.get(env):
            cfg[key] = os.environ[env]
    sel = load_json(root / ".cursor/stack-selection.json")
    if sel.get("stack") and "stack" not in cfg:
        cfg["stack"] = str(sel["stack"])
    cfg.setdefault("stack", "web")
    if not cfg.get("project_name"):
        cfg["project_name"] = root.name.replace("-", " ").title()
    if not cfg.get("purpose"):
        cfg["purpose"] = f"FOSS project built from agent-project-bootstrap ({cfg['project_name']})"
    if not cfg.get("release_repo"):
        try:
            out = subprocess.run(
                ["gh", "repo", "view", "--json", "nameWithOwner", "-q", ".nameWithOwner"],
                cwd=root,
                capture_output=True,
                text=True,
                check=False,
            )
            if out.returncode == 0 and out.stdout.strip():
                cfg["release_repo"] = out.stdout.strip()
        except FileNotFoundError:
            pass
    return cfg


def git_has_remote(root: Path) -> bool:
    if not (root / ".git").is_dir():
        return False
    out = subprocess.run(
        ["git", "remote"], cwd=root, capture_output=True, text=True, check=False
    )
    return out.returncode == 0 and bool(out.stdout.strip())


def run_cmd(root: Path, cmd: list[str], *, cwd: Path | None = None) -> tuple[int, str]:
    env = os.environ.copy()
    if os.name == "nt":
        extras = [
            Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "GitHub CLI",
            Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "nodejs",
            Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Git" / "bin",
            Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "GitHub CLI",
        ]
        prefix = os.pathsep.join(str(p) for p in extras if p.is_dir())
        if prefix:
            env["PATH"] = prefix + os.pathsep + env.get("PATH", "")
    try:
        proc = subprocess.run(
            cmd, cwd=cwd or root, capture_output=True, text=True, check=False, env=env
        )
        tail = (proc.stderr or proc.stdout or "").strip()[-400:]
        return proc.returncode, tail
    except FileNotFoundError as exc:
        return 127, str(exc)


def append_decision_log(root: Path, note: str) -> None:
    path = root / "DECISION_LOG.md"
    if not path.exists():
        path.write_text("# Decision Log\n\n", encoding="utf-8")
    ts = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    text = path.read_text(encoding="utf-8")
    path.write_text(text.rstrip() + f"\n## Autonomous /build approval ({ts})\n\n- {note}\n\n", encoding="utf-8")
