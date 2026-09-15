"""markdownlint + yamllint: run when installed; non-blocking (print only) until HARD mode."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from local_resources import in_ci


def require_tools() -> bool:
    raw = os.environ.get("REQUIRE_MDLINT", "").strip().lower()
    return raw in {"1", "true", "yes"} or in_ci()


def hard_fail() -> bool:
    return os.environ.get("MDLINT_HARD", "").strip().lower() in {"1", "true", "yes"}


def _run(cmd: list[str], cwd: Path) -> tuple[int, str]:
    try:
        proc = subprocess.run(
            cmd, cwd=cwd, capture_output=True, text=True, timeout=180, check=False
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        return 1, str(exc)
    return proc.returncode, (proc.stdout or "") + (proc.stderr or "")


def check_md_yaml(root: Path, *, which=shutil.which) -> int:
    mdlint = which("markdownlint")
    yamllint = which("yamllint")
    missing = [n for n, p in (("markdownlint", mdlint), ("yamllint", yamllint)) if not p]
    if missing:
        msg = "missing: " + ", ".join(missing)
        if require_tools():
            print(f"FAIL markdownlint/yamllint ({msg})")
            return 1
        print(f"SKIP markdownlint/yamllint ({msg}; local --quick)")
        return 0
    failed = 0
    if mdlint:
        code, out = _run(
            [mdlint, "--config", str(root / ".markdownlint.yaml"), "docs", "AGENTS.md", "README.md"],
            root,
        )
        if out.strip():
            print(out.rstrip())
        if code != 0:
            print("WARN: markdownlint (non-blocking unless MDLINT_HARD=1)")
            failed += 1
    if yamllint:
        code, out = _run(
            [yamllint, "-c", str(root / ".yamllint.yaml"), ".github"],
            root,
        )
        if out.strip():
            print(out.rstrip())
        if code != 0:
            print("WARN: yamllint (non-blocking unless MDLINT_HARD=1)")
            failed += 1
    if failed and hard_fail():
        print("FAIL: markdownlint/yamllint (MDLINT_HARD)")
        return 1
    print("OK: markdownlint/yamllint (non-blocking)")
    return 0


def main() -> int:
    return check_md_yaml(Path.cwd())


if __name__ == "__main__":
    raise SystemExit(main())
