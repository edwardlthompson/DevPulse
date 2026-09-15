"""Best-effort Discussions Ideas category."""
from __future__ import annotations

import sys

from discussions_qa import HUMAN_IDEAS, create_category, has_ideas, list_categories


def ensure_ideas(repo: str) -> str:
    if "/" not in repo:
        return HUMAN_IDEAS
    repo_id, nodes = list_categories(repo)
    if has_ideas(nodes):
        return "OK   Discussions Ideas category present"
    if create_category(repo, repo_id, "Ideas", ":bulb:"):
        return "OK   Discussions Ideas category created"
    return HUMAN_IDEAS


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    print(ensure_ideas(args[0] if args else ""))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
