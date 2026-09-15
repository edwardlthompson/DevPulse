"""Filter GitHub Actions runs so /coach ignores closed Release Please branches."""
from __future__ import annotations

import json
import shutil
import subprocess
import sys
from pathlib import Path

SKIP_PREFIXES = ("release-please--branches--",)


def _gh(args: list[str], timeout: int = 20) -> str:
    gh = shutil.which("gh")
    if not gh:
        return ""
    try:
        proc = subprocess.run(
            [gh, *args],
            capture_output=True,
            text=True,
            timeout=timeout,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return ""
    return proc.stdout if proc.returncode == 0 else ""


def load_required_names(root: Path) -> list[str]:
    path = root / ".github" / "required-checks.json"
    if not path.is_file():
        return []
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return []
    names = data.get("required_status_checks") or []
    return [str(n) for n in names if n]


def filter_runs(runs: list[dict], default_branch: str = "main") -> list[dict]:
    kept: list[dict] = []
    for run in runs:
        branch = str(run.get("headBranch") or "")
        if any(branch.startswith(p) for p in SKIP_PREFIXES):
            continue
        kept.append(run)
    if not kept:
        return [r for r in runs if str(r.get("headBranch") or "") == default_branch]
    return kept


def format_run(run: dict) -> str:
    status = run.get("status") or ""
    conclusion = run.get("conclusion") or status
    title = run.get("displayTitle") or run.get("name") or "?"
    name = run.get("name") or ""
    branch = run.get("headBranch") or ""
    return f"{status}\t{conclusion}\t{title}\t{name}\t{branch}"


def failed_required_from_runs(runs: list[dict], required: list[str]) -> list[str]:
    """Map recent failed workflow runs to required-check names (exact match on run.name)."""
    req = {n.lower(): n for n in required}
    failed: list[str] = []
    seen: set[str] = set()
    for run in runs:
        conclusion = (run.get("conclusion") or "").lower()
        if conclusion not in ("failure", "cancelled", "timed_out"):
            continue
        name = str(run.get("name") or "")
        key = name.lower()
        if key in req and key not in seen:
            seen.add(key)
            failed.append(req[key])
    return failed


def ci_red_one_liner(root: Path, runs: list[dict] | None = None) -> str:
    """One-line CI status for /resume and /coach."""
    required = load_required_names(root)
    if runs is None:
        raw = _gh(
            [
                "run",
                "list",
                "--branch",
                "main",
                "--limit",
                "30",
                "--json",
                "status,conclusion,name,displayTitle,headBranch,url,databaseId",
            ]
        )
        if not raw.strip():
            return "CI: unknown (gh unavailable)"
        try:
            runs = json.loads(raw)
        except json.JSONDecodeError:
            return "CI: unknown (bad gh JSON)"
    if not isinstance(runs, list):
        return "CI: unknown"
    shown = filter_runs(runs)
    failed = failed_required_from_runs(shown, required)
    if failed:
        return "CI red: failed required checks: " + ", ".join(failed)
    return "CI: no failed required checks on recent main runs"


def print_ci_snapshot(root: Path) -> int:
    raw = _gh(
        [
            "run",
            "list",
            "--limit",
            "20",
            "--json",
            "status,conclusion,name,displayTitle,headBranch,url,databaseId",
        ]
    )
    if not raw.strip():
        print("WARN: gh could not read workflow runs (offline or unauthenticated).")
        return 0
    try:
        runs = json.loads(raw)
    except json.JSONDecodeError:
        print("WARN: gh run list returned non-JSON.")
        return 0
    if not isinstance(runs, list):
        return 0
    shown = filter_runs(runs)[:5]
    if not shown:
        print("No recent workflow runs on non-Release-Please branches.")
    else:
        for run in shown:
            print(format_run(run))
    print(ci_red_one_liner(root, runs))
    return 0


def main(argv: list[str] | None = None) -> int:
    del argv
    return print_ci_snapshot(Path(".").resolve())


if __name__ == "__main__":
    raise SystemExit(main())
