"""CONTRIBUTING.md must include the coding-agent edition."""
from __future__ import annotations

from pathlib import Path

NEEDLES = (
    "## For coding agents",
    "AGENTS.md",
    "START_HERE.md",
    "/build",
    "watch-agent-gates",
    "git push",
    "/push",
    "/ship",
    "Conventional Commits",
    "[HUMAN]",
)


def check_repo(root: Path) -> list[str]:
    path = root / "CONTRIBUTING.md"
    if not path.is_file():
        return ["MISSING: CONTRIBUTING.md"]
    text = path.read_text(encoding="utf-8")
    return [f"CONTRIBUTING.md missing {needle}" for needle in NEEDLES if needle not in text]


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("CONTRIBUTING agent edition check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("CONTRIBUTING agent edition check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
