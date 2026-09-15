"""Devcontainer Android cmdline-tools pin. Never accept SDK licenses here."""
from __future__ import annotations

from pathlib import Path

ZIP_NAME = "commandlinetools-linux-11076708_latest.zip"
ZIP_URL = f"https://dl.google.com/android/repository/{ZIP_NAME}"
FORBIDDEN = ("yes |", "echo y |", "sdkmanager --licenses")


def scan_text(text: str) -> list[str]:
    errors: list[str] = []
    for needle in FORBIDDEN:
        if needle in text:
            errors.append(f"auto-license command forbidden: {needle}")
    return errors


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    docker = (root / ".devcontainer" / "Dockerfile").read_text(encoding="utf-8")
    if "ANDROID_HOME" not in docker:
        errors.append("Dockerfile must set ANDROID_HOME for cmdline-tools")
    if "no auto-license" not in docker.lower() and "never" not in docker.lower():
        errors.append("Dockerfile must say licenses are never auto-accepted")
    errors.extend(f"Dockerfile: {item}" for item in scan_text(docker))
    installer = root / "scripts" / "install-android-cmdline-tools.sh"
    if not installer.is_file():
        errors.append("MISSING: scripts/install-android-cmdline-tools.sh")
        return errors
    text = installer.read_text(encoding="utf-8")
    if ZIP_NAME not in text and ZIP_URL not in text:
        errors.append("install script must pin commandlinetools zip")
    errors.extend(f"install script: {item}" for item in scan_text(text))
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Android cmdline-tools check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Android cmdline-tools check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
