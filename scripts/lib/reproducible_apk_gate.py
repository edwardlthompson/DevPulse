"""Require SOURCE_DATE_EPOCH release wiring for Golden Path APKs."""

from __future__ import annotations

import sys
from pathlib import Path

EPOCH = "1700000000"


def check(root: Path) -> list[str]:
    script = root / "scripts/verify-reproducible-apk.sh"
    ci = root / ".github/workflows/ci.yml"
    readme = root / "examples/android/README.md"
    if not script.is_file():
        return []
    errors: list[str] = []
    sh = script.read_text(encoding="utf-8")
    yml = ci.read_text(encoding="utf-8") if ci.is_file() else ""
    md = readme.read_text(encoding="utf-8") if readme.is_file() else ""
    if f'SOURCE_DATE_EPOCH="${{SOURCE_DATE_EPOCH:-{EPOCH}}}"' not in sh and EPOCH not in sh:
        errors.append("verify-reproducible-apk.sh must default SOURCE_DATE_EPOCH")
    if "assembleRelease" not in sh:
        errors.append("verify-reproducible-apk.sh must assembleRelease")
    if EPOCH not in yml or "android-release" not in yml:
        errors.append("ci.yml android-release must pin SOURCE_DATE_EPOCH")
    if "HASH1" not in yml or "HASH2" not in yml:
        errors.append("ci.yml android-release must compare two APK hashes")
    if EPOCH not in md:
        errors.append("examples/android/README.md must document SOURCE_DATE_EPOCH")
    return errors


def main() -> int:
    errors = check(Path.cwd())
    if errors:
        print("\n".join(errors))
        return 1
    print("Reproducible APK gate passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
