"""Merge commercial Cloud conversation hooks without dropping FOSS guards."""
from __future__ import annotations

import json
from pathlib import Path

FOSS = Path(".cursor") / "hooks.json"
CLOUD = Path(".cursor") / "hooks.cloud.commercial.example.json"
FOSS_REQUIRED = ("beforeShellExecution", "afterFileEdit")
CLOUD_REQUIRED = ("afterAgentResponse", "stop")


def load_json(path: Path) -> dict:
    data = json.loads(path.read_text(encoding="utf-8"))
    return data if isinstance(data, dict) else {}


def merge_hooks(base: dict, extra: dict) -> dict:
    merged = {"version": base.get("version", 1), "hooks": dict(base.get("hooks") or {})}
    for name, hooks in (extra.get("hooks") or {}).items():
        current = list(merged["hooks"].get(name) or [])
        merged["hooks"][name] = current + list(hooks or [])
    return merged


def check_repo(root: Path) -> list[str]:
    foss_path = root / FOSS
    cloud_path = root / CLOUD
    errors: list[str] = []
    if not foss_path.is_file():
        return [f"MISSING: {FOSS.as_posix()}"]
    if not cloud_path.is_file():
        return [f"MISSING: {CLOUD.as_posix()}"]
    base = load_json(foss_path)
    extra = load_json(cloud_path)
    for key in FOSS_REQUIRED:
        if key not in (base.get("hooks") or {}):
            errors.append(f"{FOSS.as_posix()} missing {key}")
    if any(key in (base.get("hooks") or {}) for key in CLOUD_REQUIRED):
        errors.append(f"{FOSS.as_posix()} must not ship commercial conversation hooks")
    merged = merge_hooks(base, extra)
    hooks = merged.get("hooks") or {}
    for key in (*FOSS_REQUIRED, *CLOUD_REQUIRED):
        if key not in hooks:
            errors.append(f"merged hooks missing {key}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Cloud hook merge check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Cloud hook merge keeps FOSS shell and encoding hooks")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
