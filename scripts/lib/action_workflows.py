"""Run actionlint + zizmor on .github/workflows (offline). Skip if missing locally."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from local_resources import in_ci


def require_tools() -> bool:
    raw = os.environ.get("REQUIRE_ACTION_LINT", "").strip().lower()
    return raw in {"1", "true", "yes"} or in_ci()


def workflow_files(root: Path) -> list[Path]:
    folder = root / ".github" / "workflows"
    if not folder.is_dir():
        return []
    return sorted(p for p in folder.iterdir() if p.suffix in {".yml", ".yaml"} and p.is_file())


def _run(cmd: list[str], cwd: Path) -> tuple[int, str]:
    try:
        proc = subprocess.run(
            cmd,
            cwd=cwd,
            capture_output=True,
            text=True,
            timeout=120,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        return 1, str(exc)
    out = (proc.stdout or "") + (proc.stderr or "")
    return proc.returncode, out


def check_action_workflows(
    root: Path,
    *,
    which=shutil.which,
    runner=_run,
) -> int:
    files = workflow_files(root)
    if not files:
        print("No workflow files found")
        return 0
    actionlint = which("actionlint")
    zizmor = which("zizmor")
    missing = [name for name, path in (("actionlint", actionlint), ("zizmor", zizmor)) if not path]
    if missing:
        msg = "missing: " + ", ".join(missing)
        if require_tools():
            print(f"FAIL actionlint/zizmor ({msg})")
            print("Install both on PATH, or unset CI/REQUIRE_ACTION_LINT for a local skip.")
            return 1
        print(f"SKIP actionlint/zizmor ({msg}; local --quick)")
        return 0
    failed = 0
    if actionlint:
        # actionlint treats a bare "never" as a workflow path; omit -color.
        code, out = runner([actionlint, *map(str, files)], root)
        if out.strip():
            print(out.rstrip())
        if code != 0:
            print("FAIL: actionlint")
            failed += 1
        else:
            print("OK: actionlint")
    if zizmor:
        code, out = runner(
            [
                zizmor,
                "--offline",
                "--min-severity=medium",
                "--config",
                str(root / ".github" / "zizmor.yml"),
                str(root / ".github" / "workflows"),
            ],
            root,
        )
        if out.strip():
            print(out.rstrip())
        if code != 0:
            print("FAIL: zizmor")
            failed += 1
        else:
            print("OK: zizmor")
    return 1 if failed else 0


def main() -> int:
    return check_action_workflows(Path.cwd())


if __name__ == "__main__":
    raise SystemExit(main())
