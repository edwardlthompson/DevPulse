"""docs/CURSOR_CLI.md must keep the local no-Cloud loop recipe."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "CURSOR_CLI.md"
NEEDLES = (
    "Local loop",
    "validate-bootstrap --quick",
    "watch-agent-gates --once --autofix --scope auto",
    "CURSOR_API_KEY",
    "Do not git push",
    "workflow_dispatch",
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
        print("Cursor CLI loop check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Cursor CLI local-loop recipe check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
