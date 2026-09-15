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
    if "ci.yml" not in readme:
        errors.append("CI badge must link ci.yml")
    if is_template_repo(root):
        if "badge/license-MIT" not in readme:
            errors.append("hero license badge must be MIT")
        for label, color in OWNER_COLORS.items():
            needle = f"badge/{label}-"
            if needle not in readme or color not in readme:
                errors.append(f"owner badge {label} / {color} missing")
        for stack, color in STACK_COLORS.items():
            if f"badge/{stack}-stack-{color}" not in readme:
                errors.append(f"stack badge {stack} must use {color}")
        return errors
    cfg = {}
    cfg_path = root / "bootstrap.config.json"
    if cfg_path.is_file():
        cfg = json.loads(cfg_path.read_text(encoding="utf-8"))
    license_id = str(cfg.get("license") or "")
    if license_id:
        encoded = license_id.replace("-", "--")
        if f"badge/license-{encoded}" not in readme and f"badge/license-{license_id}" not in readme:
            errors.append(f"hero license badge must match {license_id}")
    stack = str(cfg.get("stack") or "")
    if stack and f"badge/{stack}-stack-" not in readme:
        errors.append(f"stack badge {stack} missing")
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
