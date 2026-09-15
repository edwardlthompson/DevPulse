"""docs/CURSOR_CANVAS.md stays the Canvas / Design Mode walkthrough."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "CURSOR_CANVAS.md"
NEEDLES = (
    "canvas-bootstrap-status",
    "render-gates-status",
    "Design Mode",
    "web/PWA",
    "Settings-only",
    "markdown",
    "CURSOR_CANVAS_DIR",
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
        print("Cursor Canvas walkthrough check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Cursor Canvas walkthrough check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
