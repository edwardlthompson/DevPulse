"""Forbid auto-accept of Android SDK licenses outside named CI workflows."""
from __future__ import annotations

from pathlib import Path

# Split needles so this module does not match itself.
_PARTS = (("yes | ", "sdkmanager --licenses"), ("yes|", "sdkmanager --licenses"), ("echo y | ", "sdkmanager"))
ALLOW = {
    ".github/workflows/ci.yml",
    ".github/workflows/codeql.yml",
}
SCAN_GLOBS = (
    "scripts/**/*.sh",
    "scripts/**/*.py",
    "scripts/**/*.ps1",
    ".cursor/hooks/**/*.py",
    ".devcontainer/**",
    ".github/workflows/*.yml",
)


def scan_text(text: str) -> bool:
    return any(a + b in text for a, b in _PARTS)


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    self_rel = Path(__file__).resolve().relative_to(root.resolve()).as_posix()
    for pattern in SCAN_GLOBS:
        for path in root.glob(pattern):
            if not path.is_file():
                continue
            rel = path.relative_to(root).as_posix()
            if rel == self_rel:
                continue
            try:
                body = path.read_text(encoding="utf-8")
            except (OSError, UnicodeDecodeError):
                continue
            if not scan_text(body):
                continue
            if rel in ALLOW:
                continue
            errors.append(f"{rel}: auto sdkmanager --licenses forbidden (CI allowlist only)")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Android SDK license auto-accept check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Android SDK license auto-accept check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
