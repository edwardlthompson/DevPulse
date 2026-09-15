"""`/adr` command and help twin must stay registered."""
from __future__ import annotations

from pathlib import Path

NEEDLES = ("docs/adr/", "### Critique", "docs/help/ADR.md")


def check_repo(root: Path) -> list[str]:
    cmd = root / ".cursor" / "commands" / "adr.md"
    twin = root / "docs" / "help" / "ADR.md"
    errors: list[str] = []
    if not cmd.is_file():
        errors.append("MISSING: .cursor/commands/adr.md")
    else:
        text = cmd.read_text(encoding="utf-8")
        errors.extend(f"adr.md missing {needle}" for needle in NEEDLES if needle not in text)
    if not twin.is_file():
        errors.append("MISSING: docs/help/ADR.md")
    registry = (root / "scripts" / "check-batch-commands.sh").read_text(encoding="utf-8")
    if "adr" not in registry:
        errors.append("check-batch-commands.sh must list adr")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("/adr command check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("/adr command check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
