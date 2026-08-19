"""Validate a commit message subject against Conventional Commits."""
from __future__ import annotations

import re
import sys
from pathlib import Path

TYPES = (
    "feat",
    "fix",
    "docs",
    "chore",
    "ci",
    "test",
    "refactor",
    "perf",
    "style",
    "build",
    "revert",
)
SUBJECT = re.compile(
    rf"^(?:{'|'.join(TYPES)})(?:\([A-Za-z0-9._/-]+\))?!?: .+",
)


def is_conventional(message: str) -> bool:
    subject = ""
    for line in message.splitlines():
        if line.strip():
            subject = line.strip()
            break
    if not subject:
        return False
    if subject.startswith("Merge ") or subject.startswith("Revert "):
        return True
    return bool(SUBJECT.match(subject))


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    if args:
        text = Path(args[0]).read_text(encoding="utf-8")
    else:
        text = sys.stdin.read()
    if is_conventional(text):
        return 0
    print(
        "ERROR: commit subject must be Conventional Commits "
        f"(feat|fix|docs|chore|ci|test|refactor|perf|style|build|revert): …\n"
        f"Got: {text.splitlines()[0] if text.strip() else '(empty)'}",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
