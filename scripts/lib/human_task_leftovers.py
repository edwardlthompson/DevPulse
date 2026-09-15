"""HUMAN leftover handlers that verify repo state instead of logins or devices."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

from human_task_core import AttemptResult, run_cmd
from local_resources import ollama_up

SCORECARD_NEEDLES = ("securityscorecards.dev", "api.securityscorecards.dev")
CII_NEEDLES = ("bestpractices.dev", "bestpractices.coreinfrastructure.org")


def automate_scorecard_badge(root: Path, _cfg: dict) -> AttemptResult:
    readme = (root / "README.md").read_text(encoding="utf-8")
    if not any(needle in readme for needle in SCORECARD_NEEDLES):
        return AttemptResult(1, "scorecard-badge", "README missing OpenSSF Scorecard badge", True)
    gate = (root / "scripts/pre-release-gate.sh").read_text(encoding="utf-8")
    if "--local" not in gate or "Scorecard" not in gate:
        return AttemptResult(1, "scorecard-badge", "pre-release-gate.sh must skip Scorecard on --local", True)
    if "check-security-triage.sh" not in gate:
        return AttemptResult(1, "scorecard-badge", "full gate must still call Scorecard triage", True)
    return AttemptResult(0, "scorecard-badge", "Scorecard badge present; /ship --local skips live score", False)


def automate_cii_badge(root: Path, _cfg: dict) -> AttemptResult:
    readme = (root / "README.md").read_text(encoding="utf-8")
    if any(needle in readme for needle in CII_NEEDLES):
        return AttemptResult(0, "cii-badge", "CII Best Practices badge already in README", False)
    return AttemptResult(1, "cii-badge", "CII checklist needs a human login and public badge", True)


def automate_ollama(root: Path, _cfg: dict) -> AttemptResult:
    if ollama_up():
        return AttemptResult(0, "ollama", "Ollama answered on 127.0.0.1:11434", False)
    docs = root / "docs" / "LOCAL_MODELS.md"
    hint = "docs/LOCAL_MODELS.md" if docs.is_file() else "install Ollama locally"
    return AttemptResult(1, "ollama", f"Ollama not running; optional install: {hint}", True)


def automate_crash_proxy_off(root: Path, _cfg: dict) -> AttemptResult:
    doc = root / "docs" / "CRASH_PROXY.md"
    doc_text = doc.read_text(encoding="utf-8").lower().replace("*", "") if doc.is_file() else ""
    if "not enabled" not in doc_text and "disabled" not in doc_text:
        return AttemptResult(1, "crash-proxy-off", "CRASH_PROXY.md must say the proxy is not enabled", True)
    cfg_path = root / "bootstrap.config.json"
    example = root / "bootstrap.config.json.example"
    data: dict = {}
    for path in (cfg_path, example):
        if path.is_file():
            try:
                loaded = json.loads(path.read_text(encoding="utf-8"))
            except json.JSONDecodeError:
                continue
            if isinstance(loaded, dict):
                data = loaded
                break
    if data.get("crash_proxy", {}).get("enabled") is True:
        return AttemptResult(1, "crash-proxy-off", "crash_proxy.enabled is true; DPIA required", True)
    return AttemptResult(0, "crash-proxy-off", "Crash proxy stays disabled until a HUMAN DPIA", False)


def automate_mcp_copy(root: Path, _cfg: dict) -> AttemptResult:
    src = root / ".cursor" / "mcp.foss.example"
    dst = root / ".cursor" / "mcp.json"
    if not src.is_file():
        return AttemptResult(1, "mcp-copy", ".cursor/mcp.foss.example missing", True)
    if not dst.is_file():
        shutil.copyfile(src, dst)
    return AttemptResult(0, "mcp-copy", "Gitignored .cursor/mcp.json present from FOSS example", False)


def automate_dependabot_weekly(root: Path, _cfg: dict) -> AttemptResult:
    path = root / ".github" / "dependabot.yml"
    if not path.is_file():
        return AttemptResult(1, "dependabot-weekly", ".github/dependabot.yml missing", True)
    text = path.read_text(encoding="utf-8")
    if "interval: daily" in text:
        return AttemptResult(1, "dependabot-weekly", "Dependabot still uses a daily interval", True)
    if "interval: weekly" not in text:
        return AttemptResult(1, "dependabot-weekly", "Dependabot is not weekly", True)
    return AttemptResult(0, "dependabot-weekly", "Dependabot backup is already weekly", False)


def automate_codeowners_about(root: Path, _cfg: dict) -> AttemptResult:
    path = root / ".github" / "CODEOWNERS"
    if not path.is_file():
        return AttemptResult(1, "codeowners", ".github/CODEOWNERS missing", True)
    handles = [token[1:] for token in path.read_text(encoding="utf-8").split() if token.startswith("@")]
    if not handles:
        return AttemptResult(1, "codeowners", "CODEOWNERS has no @owners", True)
    code, out = run_cmd(root, ["gh", "repo", "view", "--json", "owner", "-q", ".owner.login"])
    owner = (out or "").strip()
    if code == 0 and owner and owner not in handles:
        return AttemptResult(
            1,
            "codeowners",
            f"repo owner {owner} is not in CODEOWNERS; add as collaborator",
            True,
        )
    about = root / "scripts" / "verify-about-feature-gate.sh"
    if not about.is_file():
        return AttemptResult(1, "about-smoke", "verify-about-feature-gate.sh missing", True)
    return AttemptResult(
        0,
        "codeowners",
        "CODEOWNERS lists the repo owner; About smoke script is present",
        False,
    )
