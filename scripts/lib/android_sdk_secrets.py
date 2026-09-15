"""Fail when tracked local.properties or sdk.dir leaks into the tree."""
from __future__ import annotations

import subprocess
from pathlib import Path

GITLEAKS = Path(".gitleaks.toml")
NEEDLES = (
    "android-local-properties-sdk-dir",
    "sdk\\.dir",
    "local\\.properties",
    "adb-vendor-keys",
    "ADB_VENDOR_KEYS",
)


def tracked_local_properties(root: Path) -> list[str]:
    try:
        proc = subprocess.run(
            ["git", "-C", str(root), "ls-files", "*local.properties"],
            capture_output=True,
            text=True,
            timeout=15,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return []
    if proc.returncode != 0:
        return []
    return [line.strip() for line in proc.stdout.splitlines() if line.strip()]


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    cfg = root / GITLEAKS
    if not cfg.is_file():
        return [f"MISSING: {GITLEAKS.as_posix()}"]
    text = cfg.read_text(encoding="utf-8")
    for needle in NEEDLES:
        if needle not in text:
            errors.append(f".gitleaks.toml missing {needle}")
    for path in tracked_local_properties(root):
        errors.append(f"tracked local.properties must stay gitignored: {path}")
    ignore = (root / ".gitignore").read_text(encoding="utf-8")
    if "local.properties" not in ignore:
        errors.append(".gitignore must ignore local.properties")
    try:
        proc = subprocess.run(
            ["git", "-C", str(root), "ls-files", "*adbkey*", "*ADB_VENDOR*"],
            capture_output=True,
            text=True,
            timeout=15,
            check=False,
        )
        for path in (proc.stdout or "").splitlines():
            path = path.strip()
            if path:
                errors.append(f"tracked adb vendor key material must stay out of git: {path}")
    except (OSError, subprocess.TimeoutExpired):
        pass
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Android SDK path secret check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("local.properties / SDK path scanning ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
