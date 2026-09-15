"""hadolint on .devcontainer/Dockerfile. Skip if missing locally; required in CI."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from local_resources import in_ci

DOCKERFILE = Path(".devcontainer") / "Dockerfile"


def require_tools() -> bool:
    raw = os.environ.get("REQUIRE_HADOLINT", "").strip().lower()
    return raw in {"1", "true", "yes"} or in_ci()


def check_hadolint(root: Path, *, which=shutil.which) -> int:
    path = root / DOCKERFILE
    if not path.is_file():
        print(f"SKIP hadolint ({DOCKERFILE.as_posix()} missing)")
        return 0
    exe = which("hadolint")
    if not exe:
        if require_tools():
            print("FAIL hadolint missing on PATH")
            return 1
        print("SKIP hadolint (not on PATH; local --quick)")
        return 0
    try:
        proc = subprocess.run(
            [exe, "--failure-threshold", "error", str(path)],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=60,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"FAIL hadolint: {exc}")
        return 1
    out = (proc.stdout or "") + (proc.stderr or "")
    if out.strip():
        print(out.rstrip())
    if proc.returncode != 0:
        print("FAIL: hadolint")
        return 1
    print("OK: hadolint")
    return 0


def main() -> int:
    return check_hadolint(Path.cwd())


if __name__ == "__main__":
    raise SystemExit(main())
