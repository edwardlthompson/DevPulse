"""Require docs/MERGE_QUEUE.md to stay optional and list required checks."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "MERGE_QUEUE.md"
REQUIRED = (
    "merge queue",
    "does **not** turn on",
    "Template Upgrade Simulation (Windows)",
    "setup-github-repo.sh",
    ".github/settings.yml",
    ".github/required-checks.json",
)


def check_repo(root: Path) -> list[str]:
    path = root / DOC
    if not path.is_file():
        return [f"MISSING: {DOC.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    return [f"missing snippet: {snip}" for snip in REQUIRED if snip not in text]


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Merge queue docs check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Merge queue docs check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
