"""Discussion template must point at /ideas and docs/help/IDEAS.md."""
from __future__ import annotations

from pathlib import Path

FORM_REL = Path(".github") / "DISCUSSION_TEMPLATE" / "ideas.yml"
NEEDLES = ("/ideas", "docs/help/IDEAS.md", "Do not implement")


def check_repo(root: Path) -> list[str]:
    path = root / FORM_REL
    if not path.is_file():
        return [f"MISSING: {FORM_REL.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    return [f"{FORM_REL.as_posix()} missing {needle}" for needle in NEEDLES if needle not in text]


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Ideas discussion template check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Ideas discussion template check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
