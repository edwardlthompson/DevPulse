"""Require the Android signing/rollback runbook and env-only Gradle signing."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

REQUIRED_HEADINGS = (
    "## Upload keystore",
    "## Environment variables",
    "## Local signed release",
    "## Continuous integration",
    "## F-Droid and Play",
    "## Rollback",
)

REQUIRED_VARS = (
    "GOLDENPATH_UPLOAD_STORE_FILE",
    "GOLDENPATH_UPLOAD_STORE_PASSWORD",
    "GOLDENPATH_UPLOAD_KEY_ALIAS",
    "GOLDENPATH_UPLOAD_KEY_PASSWORD",
)

KEY_GLOBS = ("*.jks", "*.keystore", "*.p12")


def _read(root: Path, rel: str) -> str:
    path = root / rel
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def tracked_keystores(root: Path) -> list[str]:
    proc = subprocess.run(
        ["git", "-C", str(root), "ls-files", *KEY_GLOBS],
        capture_output=True,
        text=True,
        check=False,
    )
    return [line.strip() for line in proc.stdout.splitlines() if line.strip()]


def check(root: Path) -> list[str]:
    errors: list[str] = []
    runbook = _read(root, "docs/ANDROID_SIGNING.md")
    if not runbook:
        return ["docs/ANDROID_SIGNING.md is missing"]
    for heading in REQUIRED_HEADINGS:
        if heading not in runbook:
            errors.append(f"ANDROID_SIGNING.md must include {heading}")
    for var in REQUIRED_VARS:
        if var not in runbook:
            errors.append(f"ANDROID_SIGNING.md must document {var}")
    if "never commit" not in runbook.lower():
        errors.append("ANDROID_SIGNING.md must say never commit keystores")
    if "mapping.txt" not in runbook:
        errors.append("ANDROID_SIGNING.md must mention mapping.txt")
    ops = _read(root, "docs/RUNBOOK.md")
    if "ANDROID_SIGNING.md" not in ops:
        errors.append("docs/RUNBOOK.md must link ANDROID_SIGNING.md")
    android = root / "examples/android"
    if android.is_dir():
        readme = _read(root, "examples/android/README.md")
        if "ANDROID_SIGNING.md" not in readme:
            errors.append("examples/android/README.md must link ANDROID_SIGNING.md")
        gradle = _read(root, "examples/android/app/build.gradle.kts")
        if "GOLDENPATH_UPLOAD_STORE_FILE" not in gradle:
            errors.append("app/build.gradle.kts must read GOLDENPATH_UPLOAD_STORE_FILE")
        if 'storePassword = "' in gradle or "storePassword = '" in gradle:
            errors.append("app/build.gradle.kts must not hardcode storePassword")
    ignore = _read(root, ".gitignore")
    for pattern in KEY_GLOBS:
        if pattern not in ignore:
            errors.append(f".gitignore must include {pattern}")
    gate = _read(root, "scripts/feature-gate.sh")
    if "check-android-signing-runbook.sh" not in gate:
        errors.append("feature-gate.sh must run check-android-signing-runbook.sh")
    errors.extend(f"tracked keystore: {p}" for p in tracked_keystores(root))
    return errors


def main() -> int:
    errors = check(Path.cwd())
    if errors:
        print("\n".join(errors))
        return 1
    print("Android signing runbook gate passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
