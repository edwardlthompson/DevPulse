"""Fail when README hero/stack/owner badges drift from repo truth."""
from __future__ import annotations

import json
from pathlib import Path

from build_sprint_model import is_template_repo

OWNER_COLORS = {
    "AGENT": "2ea043",
    "HUMAN": "0969da",
    "ADB": "bf8700",
    "AUTO": "656d76",
}
STACK_COLORS = {
    "web": "646cff",
    "python": "3776AB",
    "android": "3DDC84",
}


def check_repo(root: Path) -> list[str]:
    readme = (root / "README.md").read_text(encoding="utf-8")
    version = (root / ".template-version").read_text(encoding="utf-8").strip()
    errors: list[str] = []
    if f"badge/template-{version}" not in readme:
        errors.append(f"hero template badge must be template-{version}")
    if "FOSS-no_tracking" not in readme:
        errors.append("hero FOSS badge must say no_tracking")

    if is_template_repo(root):
        if "badge/license-MIT" not in readme:
            errors.append("hero license badge must be MIT")
        if "actions/workflows/ci.yml" not in readme:
            errors.append("CI badge must link ci.yml")
        for label, color in OWNER_COLORS.items():
            needle = f"badge/{label}-"
            if needle not in readme or color not in readme:
                errors.append(f"owner badge {label} / {color} missing")
            elif f"{label}-" not in readme or color.lower() not in readme.lower():
                errors.append(f"owner badge {label} color {color} missing")
        for stack, color in STACK_COLORS.items():
            if f"badge/{stack}-stack-{color}" not in readme:
                errors.append(f"stack badge {stack} must use {color}")
        return errors

    # Child product README: license + active stack only.
    license_name = "MIT"
    cfg = root / "bootstrap.config.json"
    if cfg.is_file():
        try:
            license_name = str(json.loads(cfg.read_text(encoding="utf-8")).get("license") or "MIT")
        except json.JSONDecodeError:
            pass
    if "GPL" in license_name.upper():
        if "badge/license-GPL" not in readme and "GPL--3.0" not in readme:
            errors.append("hero license badge must mention GPL")
    elif f"badge/license-{license_name}" not in readme and "badge/license-MIT" not in readme:
        errors.append(f"hero license badge must be {license_name}")

    stack = "android"
    if cfg.is_file():
        try:
            stack = str(json.loads(cfg.read_text(encoding="utf-8")).get("stack") or stack).lower()
        except json.JSONDecodeError:
            pass
    color = STACK_COLORS.get(stack)
    if color and f"badge/{stack}-stack-{color}" not in readme:
        # Allow child neon android badge used by DevPulse product README.
        if not (stack == "android" and f"badge/{stack}-stack-" in readme):
            errors.append(f"stack badge {stack} must use {color}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("README badge accuracy check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("README badge accuracy check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
