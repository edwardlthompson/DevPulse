"""Docs / packaging HUMAN waiting-row handlers."""
from __future__ import annotations

import json
import os
import subprocess
from pathlib import Path

from human_task_core import AttemptResult, run_cmd
from human_task_github import bash_script


def automate_openssf_gap_list(root: Path, _cfg: dict) -> AttemptResult:
    """Write Baseline-1 / Silver gap list without claiming unmet Silver criteria."""
    src = root / ".bestpractices.json"
    if not src.is_file():
        return AttemptResult(1, "openssf-gap", ".bestpractices.json missing", True)
    try:
        data = json.loads(src.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return AttemptResult(1, "openssf-gap", f"invalid JSON: {exc}", True)
    if not isinstance(data, dict):
        return AttemptResult(1, "openssf-gap", ".bestpractices.json not an object", True)

    baseline = sorted(
        k for k in data if str(k).upper().startswith("OSPS-") or str(k).lower().startswith("osps_")
    )
    silver = sorted(
        k
        for k in data
        if str(k).startswith(("code_of_conduct", "documentation_security", "documentation_quick"))
    )
    out = root / "docs" / "OPENSSF_GAP_LIST.md"
    lines = [
        "# OpenSSF Baseline-1 / Silver gap list",
        "",
        "> Generated from `.bestpractices.json`. Do **not** mark Silver/Gold Met unless true.",
        "",
        "## Live badge",
        "",
        "- Passing: [project 14564](https://www.bestpractices.dev/en/projects/14564)",
        "- Apply URLs: `python3 scripts/lib/bestpractices_apply.py`",
        "",
        "## Baseline-1 keys",
        "",
    ]
    lines.extend(f"- `{k}`" for k in baseline) if baseline else lines.append("- _(none)_")
    lines.extend(["", "## Silver keys (proposal only)", ""])
    lines.extend(f"- `{k}`" for k in silver) if silver else lines.append("- _(none)_")
    lines.extend(
        [
            "",
            "## Do not claim Met yet",
            "",
            "- two-person review / required reviewers (unless true)",
            "- signed git tags on every release (unless true)",
            "- 80–90% coverage claims (unless measured)",
            "",
            "## Human follow-up (optional)",
            "",
            "1. Save Baseline-1 apply URLs from `bestpractices_apply.py`.",
            "2. Confirm branch protection before OSPS-AC-03.* Met.",
            "3. Leave Silver/Gold unchecked until criteria are real.",
            "",
        ]
    )
    out.write_text("\n".join(lines), encoding="utf-8")
    return AttemptResult(0, "openssf-gap", f"Wrote {out.relative_to(root)} (no fake Silver claims)", False)


def automate_winget_checklist(root: Path, _cfg: dict) -> AttemptResult:
    """Validate Winget example + dry-run stub; never submit to winget-pkgs."""
    example = root / "packaging" / "winget" / "example" / "manifest.yaml"
    if not example.is_file():
        return AttemptResult(1, "winget-checklist", "packaging/winget/example/manifest.yaml missing", True)
    code, tail = run_cmd(root, bash_script(root, "scripts/validate-winget-stub.sh", str(example)))
    if code != 0:
        return AttemptResult(1, "winget-checklist", tail or f"validate exit {code}", True)

    out_dir = root / "dist" / "winget-loop"
    out_dir.mkdir(parents=True, exist_ok=True)
    x64 = out_dir / "goldenpath-dry-x64.zip"
    arm64 = out_dir / "goldenpath-dry-arm64.zip"
    for path, label in ((x64, "x64"), (arm64, "arm64")):
        readme = out_dir / f"README-{label}.txt"
        readme.write_text(f"Golden Path Winget dry-run {label}\n", encoding="utf-8")
        zcode, ztail = run_cmd(
            root,
            ["bash", "-lc", f"cd {out_dir.as_posix()!r} && zip -q -X {path.name!r} {readme.name!r}"],
        )
        if zcode != 0:
            tcode, ttail = run_cmd(root, ["tar", "-C", str(out_dir), "-cf", str(path), readme.name])
            if tcode != 0:
                return AttemptResult(1, "winget-checklist", ztail or ttail or "pack failed", True)

    env = os.environ.copy()
    env["WINGET_INSTALLER_X64"] = str(x64)
    env["WINGET_INSTALLER_ARM64"] = str(arm64)
    proc = subprocess.run(
        bash_script(root, "scripts/winget-publish-loop.sh", "--dry-run"),
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
        env=env,
    )
    if proc.returncode != 0:
        return AttemptResult(1, "winget-checklist", (proc.stderr or proc.stdout or "")[-400:], True)
    stub = out_dir / "manifest.stub.yaml"
    vcode, vtail = run_cmd(root, bash_script(root, "scripts/validate-winget-stub.sh", str(stub)))
    if vcode != 0:
        return AttemptResult(1, "winget-checklist", vtail or f"stub validate exit {vcode}", True)
    return AttemptResult(
        0,
        "winget-checklist",
        "Winget example + dry-run stub validated; never submitted to winget-pkgs",
        False,
    )


def automate_lightroom_smoke(root: Path, _cfg: dict) -> AttemptResult:
    """Lua/SDK gates replace Adobe Plug-in Manager load for the Golden Path stub."""
    for rel in (
        "scripts/verify-lightroom.sh",
        "scripts/check-lightroom-lua.sh",
        "scripts/check-lightroom-sdk-playbook.sh",
        "scripts/check-lightroom-tagset-fuzz.sh",
    ):
        if not (root / rel).is_file():
            continue
        code, tail = run_cmd(root, bash_script(root, rel))
        if code != 0:
            return AttemptResult(1, "lightroom", tail or f"{rel} exit {code}", True)
    return AttemptResult(
        0,
        "lightroom",
        "Lightroom stub gates passed (Lua/SDK); Adobe host not required",
        False,
    )
