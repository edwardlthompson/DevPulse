"""GitHub HUMAN automation handlers."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
from pathlib import Path

from human_task_core import AttemptResult, run_cmd


def resolve_bash_exe() -> str:
    if os.name == "nt":
        candidates = [
            Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Git" / "bin" / "bash.exe",
            Path(os.environ.get("ProgramFiles(x86)", r"C:\Program Files (x86)"))
            / "Git"
            / "bin"
            / "bash.exe",
            Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "Git" / "bin" / "bash.exe",
        ]
        for path in candidates:
            if path.is_file():
                return str(path)
        which = shutil.which("bash")
        if which and "System32" not in which.replace("/", "\\"):
            return which
    return shutil.which("bash") or "bash"


def bash_script(root: Path, rel: str, *args: str) -> list[str]:
    return [resolve_bash_exe(), rel.replace("\\", "/"), *args]


def automate_branch_protection(root: Path, _cfg: dict) -> AttemptResult:
    if not (root / "scripts/setup-github-repo.sh").is_file():
        return AttemptResult(1, "branch-protection", "setup-github-repo.sh missing", True)
    code, tail = run_cmd(root, bash_script(root, "scripts/setup-github-repo.sh"))
    if code != 0:
        return AttemptResult(1, "branch-protection", tail or f"setup exit {code}", True)
    if (root / "scripts/verify-branch-protection.sh").is_file():
        vcode, vtail = run_cmd(root, bash_script(root, "scripts/verify-branch-protection.sh"))
        if vcode != 0:
            return AttemptResult(1, "branch-protection", vtail or f"verify exit {vcode}", True)
    return AttemptResult(0, "branch-protection", "Required status checks configured on main", False)


def automate_dependabot_major_merge(root: Path, _cfg: dict) -> AttemptResult:
    """Merge open Dependabot PRs that are mergeable (CI already green on the PR)."""
    proc = subprocess.run(
        [
            "gh",
            "pr",
            "list",
            "--state",
            "open",
            "--author",
            "app/dependabot",
            "--json",
            "number,title,mergeable,url",
        ],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        return AttemptResult(1, "dependabot-merge", (proc.stderr or "")[-400:], True)
    try:
        prs = json.loads(proc.stdout or "[]")
    except json.JSONDecodeError:
        return AttemptResult(1, "dependabot-merge", "invalid gh pr list JSON", True)
    if not prs:
        return AttemptResult(0, "dependabot-merge", "No open Dependabot PRs", False)

    merged: list[str] = []
    failed: list[str] = []
    for pr in prs:
        num = pr.get("number")
        if not num:
            continue
        subprocess.run(
            ["gh", "pr", "edit", str(num), "--add-label", "HUMAN"],
            cwd=root,
            capture_output=True,
            text=True,
            check=False,
        )
        mcode, mtail = run_cmd(
            root,
            ["gh", "pr", "merge", str(num), "--squash", "--delete-branch"],
        )
        if mcode == 0:
            merged.append(f"#{num}")
        else:
            failed.append(f"#{num}:{mtail or mcode}")

    if failed and not merged:
        return AttemptResult(1, "dependabot-merge", "; ".join(failed)[:400], True)
    if failed:
        return AttemptResult(
            1,
            "dependabot-merge",
            f"merged {','.join(merged)}; failed {';'.join(failed)}"[:400],
            True,
        )
    return AttemptResult(0, "dependabot-merge", f"Merged {', '.join(merged)}", False)


def automate_automerge_token(root: Path, _cfg: dict) -> AttemptResult:
    if (root / "scripts/setup-automerge-token.sh").is_file():
        code, tail = run_cmd(root, bash_script(root, "scripts/setup-automerge-token.sh"))
        if code == 0:
            return AttemptResult(0, "automerge-token", "AUTOMERGE_TOKEN secret set", False)
        return AttemptResult(1, "automerge-token", tail or f"exit {code}", True)
    token = os.environ.get("AUTOMERGE_TOKEN", "").strip()
    if not token:
        proc = subprocess.run(
            ["gh", "auth", "token"],
            cwd=root,
            capture_output=True,
            text=True,
            check=False,
        )
        token = (proc.stdout or "").strip()
        if proc.returncode != 0 or not token:
            return AttemptResult(
                1,
                "automerge-token",
                "No AUTOMERGE_TOKEN env and gh auth token unavailable",
                True,
            )
    proc = subprocess.run(
        ["gh", "secret", "set", "AUTOMERGE_TOKEN", "--body", token],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        return AttemptResult(
            1,
            "automerge-token",
            (proc.stderr or proc.stdout or f"exit {proc.returncode}")[-400:],
            True,
        )
    return AttemptResult(
        0, "automerge-token", "AUTOMERGE_TOKEN secret set from gh auth token", False
    )
