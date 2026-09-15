"""reuse lint when the reuse CLI is installed. Skip locally if missing."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from local_resources import in_ci


def require_tools() -> bool:
    raw = os.environ.get("REQUIRE_REUSE", "").strip().lower()
    return raw in {"1", "true", "yes"} or in_ci()


def check_reuse(root: Path, *, which=shutil.which) -> int:
    if not (root / "REUSE.toml").is_file():
        print("FAIL: REUSE.toml missing")
        return 1
    if not (root / "LICENSES" / "MIT.txt").is_file():
        print("FAIL: LICENSES/MIT.txt missing")
        return 1
    exe = which("reuse")
    if not exe:
        if require_tools():
            print("FAIL reuse CLI missing on PATH")
            return 1
        print("SKIP reuse lint (CLI not on PATH; local --quick)")
        return 0
    try:
        proc = subprocess.run(
            [exe, "lint"],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=120,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"FAIL reuse: {exc}")
        return 1
    out = (proc.stdout or "") + (proc.stderr or "")
    if out.strip():
        print(out.rstrip())
    if proc.returncode != 0:
        print("FAIL: reuse lint")
        return 1
    print("OK: reuse lint")
    return 0


def main() -> int:
    return check_reuse(Path.cwd())


if __name__ == "__main__":
    raise SystemExit(main())
