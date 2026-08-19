"""Optional post-bootstrap actions (off by default)."""
from __future__ import annotations

import subprocess
from pathlib import Path

from bootstrap_engine import tool_present


def ensure_git_repo(root: Path) -> str:
    if (root / ".git").exists():
        return "git repo already present"
    if not tool_present("git"):
        raise RuntimeError("git is required. Install Git and retry.")
    proc = subprocess.run(
        ["git", "init"], cwd=root, capture_output=True, text=True, check=False
    )
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or "git init failed")
    return "ran git init"


def install_deps(root: Path, stack: str) -> list[str]:
    notes: list[str] = []
    jobs: list[tuple[str, list[str], Path]] = []
    if stack in ("web", "multi", "none") and (root / "examples/web/package-lock.json").is_file():
        jobs.append(("web", ["npm", "ci"], root / "examples/web"))
    if stack in ("node", "multi", "none") and (root / "examples/node/package-lock.json").is_file():
        jobs.append(("node", ["npm", "ci"], root / "examples/node"))
    if stack in ("python", "multi", "none") and (root / "examples/python/pyproject.toml").is_file():
        jobs.append(("python", ["uv", "sync", "--locked", "--all-extras"], root / "examples/python"))
    for label, cmd, cwd in jobs:
        if not tool_present(cmd[0]):
            raise RuntimeError(f"{cmd[0]} is required to install {label} deps")
        proc = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, check=False)
        if proc.returncode != 0:
            tail = (proc.stderr or proc.stdout or "").strip().splitlines()[-1:]
            raise RuntimeError(f"{label} install failed: {tail[0] if tail else cmd}")
        notes.append(f"installed {label} deps")
    if not notes:
        notes.append("no stack lockfiles to install")
    return notes


def run_stack_tests(root: Path, stack: str) -> list[str]:
    notes: list[str] = []
    jobs: list[tuple[str, list[str], Path]] = []
    if stack in ("web", "multi") and (root / "examples/web/package.json").is_file():
        jobs.append(("web", ["npm", "test"], root / "examples/web"))
    if stack in ("python", "multi") and (root / "examples/python/pyproject.toml").is_file():
        jobs.append(("python", ["uv", "run", "pytest"], root / "examples/python"))
    if stack in ("node", "multi") and (root / "examples/node/package.json").is_file():
        jobs.append(("node", ["npm", "test"], root / "examples/node"))
    for label, cmd, cwd in jobs:
        if not tool_present(cmd[0]):
            raise RuntimeError(f"{cmd[0]} is required to test {label}")
        proc = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, check=False)
        if proc.returncode != 0:
            raise RuntimeError(f"{label} tests failed (exit {proc.returncode})")
        notes.append(f"{label} tests passed")
    if not notes:
        notes.append("no automated stack tests selected")
    return notes


def create_welcome_issue(root: Path) -> str:
    if not tool_present("gh"):
        raise RuntimeError(
            "gh is required for post_welcome_issue. Install GitHub CLI or disable the hook."
        )
    title = "Welcome: take the 10-minute tour"
    body = (
        "This issue is a first-run pointer, not a bug.\n\n"
        "- Tour: `docs/help/TOUR.md` (Cursor: `/tour`)\n"
        "- Glossary: `docs/help/GLOSSARY.md`\n"
        "- Start here: `docs/START_HERE.md`\n\n"
        "Close this issue after you finish the tour."
    )
    proc = subprocess.run(
        ["gh", "issue", "create", "--title", title, "--body", body],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or "gh issue create failed")
    url = (proc.stdout or "").strip()
    return f"opened welcome issue: {url}" if url else "opened welcome issue"
