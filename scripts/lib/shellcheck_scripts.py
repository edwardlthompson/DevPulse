"""shellcheck -S error on scripts/*.sh. Skip if missing locally; required in CI."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from local_resources import in_ci


def require_tools() -> bool:
    raw = os.environ.get("REQUIRE_SHELLCHECK", "").strip().lower()
    return raw in {"1", "true", "yes"} or in_ci()


def script_files(root: Path) -> list[Path]:
    folder = root / "scripts"
    if not folder.is_dir():
        return []
    return sorted(p for p in folder.glob("*.sh") if p.is_file())


def check_shellcheck(root: Path, *, which=shutil.which, runner=None) -> int:
    files = script_files(root)
    if not files:
        print("No scripts/*.sh found")
        return 0
    exe = which("shellcheck")
    if not exe:
        if require_tools():
            print("FAIL shellcheck missing on PATH")
            return 1
        print("SKIP shellcheck (not on PATH; local --quick)")
        return 0
    cmd = [exe, "-S", "error", *[str(p) for p in files]]
    if runner is None:
        try:
            proc = subprocess.run(
                cmd,
                cwd=root,
                capture_output=True,
                text=True,
                timeout=120,
                check=False,
            )
        except (OSError, subprocess.TimeoutExpired) as exc:
            print(f"FAIL shellcheck: {exc}")
            return 1
        out = (proc.stdout or "") + (proc.stderr or "")
        code = proc.returncode
    else:
        code, out = runner(cmd, root)
    if out.strip():
        print(out.rstrip())
    if code != 0:
        print("FAIL: shellcheck")
        return 1
    print(f"OK: shellcheck ({len(files)} scripts, -S error)")
    return 0


def main() -> int:
    return check_shellcheck(Path.cwd())


if __name__ == "__main__":
    raise SystemExit(main())
