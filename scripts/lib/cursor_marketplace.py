"""docs/CURSOR_MARKETPLACE.md stays the FOSS-default marketplace runbook."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "CURSOR_MARKETPLACE.md"
NEEDLES = (
    "pack-cursor-plugin",
    "dist/cursor-plugin",
    "wshobson/agents",
    "Do **not** install",
    "[HUMAN]",
    "unsigned marketplace",
)


def check_repo(root: Path) -> list[str]:
    path = root / DOC
    if not path.is_file():
        return [f"MISSING: {DOC.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    return [f"{DOC.as_posix()} missing {needle}" for needle in NEEDLES if needle not in text]


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Cursor marketplace runbook check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Cursor marketplace runbook check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
