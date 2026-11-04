"""ADB / Android HUMAN automation handlers."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from human_task_core import AttemptResult, run_cmd


def _resolve_adb() -> str:
    adb = os.environ.get("ADB", "adb")
    if shutil.which(adb):
        return adb
    if os.name == "nt":
        win = os.environ.get("LOCALAPPDATA", "")
        if win:
            candidate = Path(win) / "Android/Sdk/platform-tools/adb.exe"
            if candidate.is_file():
                return str(candidate)
    return adb


def adb_authorized(root: Path) -> bool:
    adb = _resolve_adb()
    try:
        out = subprocess.run(
            [adb, "devices"],
            capture_output=True,
            text=True,
            check=False,
        )
    except FileNotFoundError:
        return False
    if out.returncode != 0:
        return False
    for line in out.stdout.splitlines()[1:]:
        if line.strip().endswith("device"):
            return True
    return False


def _gradle_argv(root: Path, *tasks: str) -> list[str] | None:
    android = root / "examples" / "android"
    if os.name == "nt":
        bat = android / "gradlew.bat"
        if bat.is_file():
            return ["cmd", "/c", str(bat), *tasks]
    gradlew = android / "gradlew"
    if not gradlew.is_file():
        return None
    # Git Bash mangles backslashes; prefer forward-slash path.
    return ["bash", gradlew.as_posix(), *tasks]


def _posix_for_bash(path: Path) -> str:
    """Convert a Windows path to a Git Bash / MSYS style path when needed."""
    resolved = path.resolve()
    text = resolved.as_posix()
    if os.name == "nt" and len(text) >= 2 and text[1] == ":":
        return f"/{text[0].lower()}{text[2:]}"
    return text


def automate_adb_instrumented(root: Path, _cfg: dict) -> AttemptResult:
    if adb_authorized(root):
        # Prefer Gradle on Windows — bare `bash` may be WSL and miss the repo path.
        argv = _gradle_argv(root, "connectedDebugAndroidTest")
        if argv:
            code, tail = run_cmd(root, argv, cwd=root / "examples/android")
            if code == 0:
                return AttemptResult(0, "connectedDebugAndroidTest", "connectedDebugAndroidTest passed", False)
            return AttemptResult(1, "connectedDebugAndroidTest", tail or f"exit {code}", True)
        verify = root / "scripts/verify-android-insets.sh"
        if verify.is_file():
            code, tail = run_cmd(root, ["bash", _posix_for_bash(verify)])
            if code == 0:
                return AttemptResult(0, "verify-android-insets", "ADB instrumented tests passed", False)
            return AttemptResult(1, "verify-android-insets", tail or f"exit {code}", True)
    argv = _gradle_argv(root, "test")
    if argv:
        run_cmd(root, argv, cwd=root / "examples/android")
    return AttemptResult(
        1,
        "adb-unavailable",
        "no_authorized_device; unit tests run if Android tree present",
        True,
    )


def automate_fdroid_dry_run(root: Path, _cfg: dict) -> AttemptResult:
    script = root / "scripts/fdroid-device-dry-run.sh"
    if not script.is_file():
        return AttemptResult(1, "fdroid-dry-run", "fdroid-device-dry-run.sh missing", True)
    if not adb_authorized(root):
        return AttemptResult(1, "fdroid-dry-run", "no_authorized_device", True)
    code, tail = run_cmd(root, ["bash", _posix_for_bash(script)])
    if code == 0:
        return AttemptResult(0, "fdroid-dry-run", "F-Droid device dry-run passed", False)
    return AttemptResult(1, "fdroid-dry-run", tail or f"exit {code}", True)


def automate_android_sdk_smoke(root: Path, _cfg: dict) -> AttemptResult:
    argv = _gradle_argv(root, "test")
    if argv:
        code, tail = run_cmd(root, argv, cwd=root / "examples/android")
        if code != 0:
            return AttemptResult(1, "gradle-test", tail or f"exit {code}", True)
    if adb_authorized(root):
        adb = _resolve_adb()
        code, _ = run_cmd(root, [adb, "shell", "getprop", "ro.build.version.sdk"])
        if code == 0:
            return AttemptResult(0, "adb-getprop", "Gradle tests + adb getprop smoke", False)
    if argv:
        return AttemptResult(1, "adb-unavailable", "no_authorized_device after unit tests", True)
    return AttemptResult(1, "android-sdk", "No Android example tree", True)
