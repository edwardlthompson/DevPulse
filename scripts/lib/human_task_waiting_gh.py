"""GitHub Settings checks for waiting-on-a-person HUMAN rows."""
from __future__ import annotations

import json
import subprocess
from pathlib import Path

from human_task_core import AttemptResult


def _gh_json(root: Path, *args: str) -> tuple[int, object]:
    proc = subprocess.run(
        ["gh", *args],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        return proc.returncode, (proc.stderr or proc.stdout or "").strip()[-400:]
    try:
        return 0, json.loads(proc.stdout or "null")
    except json.JSONDecodeError:
        return 1, "invalid gh JSON"


def automate_private_vuln_reporting(root: Path, _cfg: dict) -> AttemptResult:
    """Confirm private vulnerability reporting is enabled."""
    code, data = _gh_json(root, "api", "repos/{owner}/{repo}/private-vulnerability-reporting")
    if code != 0:
        return AttemptResult(1, "private-vuln", str(data), True)
    if not isinstance(data, dict) or data.get("enabled") is not True:
        return AttemptResult(
            1,
            "private-vuln",
            "private vulnerability reporting is off; run scripts/setup-github-repo.sh",
            True,
        )
    advisory = root / "SECURITY.md"
    if advisory.is_file():
        text = advisory.read_text(encoding="utf-8")
        if "security/advisories" not in text and "Private vulnerability" not in text:
            return AttemptResult(1, "private-vuln", "SECURITY.md missing advisories path", True)
    return AttemptResult(
        0,
        "private-vuln",
        "Private vulnerability reporting enabled; SECURITY.md documents the path",
        False,
    )


def automate_push_protection(root: Path, _cfg: dict) -> AttemptResult:
    proc = subprocess.run(
        [
            "gh",
            "api",
            "repos/{owner}/{repo}",
            "--jq",
            ".security_and_analysis.secret_scanning_push_protection.status",
        ],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        return AttemptResult(1, "push-protection", (proc.stderr or proc.stdout or "")[-400:], True)
    status = (proc.stdout or "").strip().strip('"')
    if status != "enabled":
        return AttemptResult(
            1,
            "push-protection",
            f"secret_scanning_push_protection={status or 'unknown'}; enable in Settings",
            True,
        )
    return AttemptResult(0, "push-protection", "Secret scanning push protection enabled", False)


def automate_pages_custom_domain(root: Path, _cfg: dict) -> AttemptResult:
    """Optional Pages custom domain checklist — pass when Pages is up."""
    code, data = _gh_json(root, "api", "repos/{owner}/{repo}/pages")
    if code != 0:
        return AttemptResult(1, "pages-domain", str(data), True)
    if not isinstance(data, dict):
        return AttemptResult(1, "pages-domain", "unexpected pages payload", True)
    html = data.get("html_url") or ""
    cname = data.get("cname")
    https = data.get("https_enforced")
    note = f"Pages live at {html}; https_enforced={https}"
    if cname:
        note += f"; custom domain {cname}"
    else:
        note += "; no custom domain (optional for this template)"
    return AttemptResult(0, "pages-domain", note, False)
