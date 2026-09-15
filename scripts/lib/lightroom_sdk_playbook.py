"""Keep the Lightroom SDK bump playbook and version table aligned."""

from __future__ import annotations

import re
import sys
from pathlib import Path

HEADINGS = (
    "## Current versions",
    "## When to bump",
    "## Steps",
    "## Optional host smoke (Adobe app)",
    "## Do not",
)
INFO_VER = re.compile(r"LrSdkVersion\s*=\s*([0-9.]+)")
INFO_MIN = re.compile(r"LrSdkMinimumVersion\s*=\s*([0-9.]+)")
README_VER = re.compile(r"`LrSdkVersion`\s*\|\s*\*\*([0-9.]+)\*\*")
README_MIN = re.compile(r"`LrSdkMinimumVersion`\s*\|\s*\*\*([0-9.]+)\*\*")


def _read(root: Path, rel: str) -> str:
    path = root / rel
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def _first(pattern: re.Pattern[str], text: str) -> str:
    match = pattern.search(text)
    return match.group(1) if match else ""


def check(root: Path) -> list[str]:
    if not (root / "examples/lightroom/Info.lua").is_file():
        return []
    errors: list[str] = []
    playbook = _read(root, "docs/LIGHTROOM_SDK_BUMP.md")
    if not playbook:
        return ["docs/LIGHTROOM_SDK_BUMP.md is missing"]
    for heading in HEADINGS:
        if heading not in playbook:
            errors.append(f"LIGHTROOM_SDK_BUMP.md must include {heading}")
    if "Plug-in Manager" not in playbook:
        errors.append("LIGHTROOM_SDK_BUMP.md must mention Plug-in Manager")
    info = _read(root, "examples/lightroom/Info.lua")
    readme = _read(root, "examples/lightroom/README.md")
    pairs = (
        ("LrSdkVersion", _first(INFO_VER, info), _first(README_VER, readme)),
        ("LrSdkMinimumVersion", _first(INFO_MIN, info), _first(README_MIN, readme)),
    )
    for name, left, right in pairs:
        if not left or not right:
            errors.append(f"{name} missing in Info.lua or README")
        elif left != right:
            errors.append(f"{name} mismatch: Info.lua={left} README={right}")
    pin = root / "examples/lightroom/SDK_PIN.sha256"
    info_path = root / "examples/lightroom/Info.lua"
    if pin.is_file() and info_path.is_file():
        import hashlib

        digest = hashlib.sha256(info_path.read_bytes()).hexdigest()
        expected = pin.read_text(encoding="utf-8").strip().split()[0]
        if digest != expected:
            errors.append(f"Info.lua sha256 {digest} != SDK_PIN.sha256 {expected}")
    elif info_path.is_file():
        errors.append("examples/lightroom/SDK_PIN.sha256 missing (pin Info.lua checksum)")
    gate = _read(root, "scripts/feature-gate.sh")
    if "check-lightroom-sdk-playbook.sh" not in gate:
        errors.append("feature-gate.sh must run check-lightroom-sdk-playbook.sh")
    return errors


def main() -> int:
    errors = check(Path.cwd())
    if errors:
        print("\n".join(errors))
        return 1
    print("Lightroom SDK playbook gate passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
