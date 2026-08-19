"""Validate TEMPLATE_INDEX.json paths (Python-only; avoids jq.exe CRLF)."""
from __future__ import annotations

import glob
import json
import sys
from pathlib import Path


def _missing(root: Path, rel: str) -> bool:
    return not (root / rel).exists()


def collect_missing(root: Path, data: dict) -> list[str]:
    errors: list[str] = []
    for ep in data.get("entry_points", {}).values():
        if _missing(root, ep):
            errors.append(ep)
    for item in data.get("files", []):
        path = item.get("path", "")
        if path and _missing(root, path):
            errors.append(path)
    for mod in data.get("modules", {}).values():
        for key in ("guide", "example"):
            rel = mod.get(key)
            if rel and _missing(root, rel):
                errors.append(rel)
    return errors


def collect_unindexed(root: Path, data: dict) -> list[str]:
    indexed = {item["path"] for item in data.get("files", [])}
    extra: list[str] = []
    for sh in sorted(glob.glob(str(root / "scripts" / "*.sh"))):
        rel = Path(sh).relative_to(root).as_posix()
        if rel not in indexed:
            extra.append(rel)
    for wf in sorted(glob.glob(str(root / ".github" / "workflows" / "*.yml"))):
        rel = Path(wf).relative_to(root).as_posix()
        if rel not in indexed:
            extra.append(rel)
    return extra


def check(root: Path) -> list[str]:
    index = root / "TEMPLATE_INDEX.json"
    data = json.loads(index.read_text(encoding="utf-8"))
    errors = [f"MISSING: {p}" for p in collect_missing(root, data)]
    errors.extend(f"UNINDEXED: {p}" for p in collect_unindexed(root, data))
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    root = Path(args[0] if args else ".").resolve()
    errors = check(root)
    if errors:
        for item in errors[:40]:
            print(item)
        extra = len(errors) - 40
        if extra > 0:
            print(f"{extra} more path error(s)")
        return 1
    print("TEMPLATE_INDEX.json validation passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
