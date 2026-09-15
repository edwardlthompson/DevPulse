"""Tour and Coach must teach Settings-only home chrome."""
from __future__ import annotations

from pathlib import Path

DOCS = (Path("docs") / "help" / "TOUR.md", Path("docs") / "help" / "COACH.md")
NEEDLES = ("Settings-only", "Theme", "Settings/About")


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    for rel in DOCS:
        path = root / rel
        if not path.is_file():
            errors.append(f"MISSING: {rel.as_posix()}")
            continue
        text = path.read_text(encoding="utf-8")
        errors.extend(f"{rel.as_posix()} missing {needle}" for needle in NEEDLES if needle not in text)
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Tour/Coach chrome check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Tour/Coach Settings-only chrome check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
