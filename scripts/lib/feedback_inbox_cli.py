"""CLI entry for feedback-inbox."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from feedback_inbox import classify_issues, fetch_issues


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    root = Path.cwd()
    board = ""
    for name in ("BUILD_PLAN.md", "COMPLETED_TASKS.md"):
        path = root / name
        if path.is_file():
            board += path.read_text(encoding="utf-8")
    if "--fixture" in args:
        idx = args.index("--fixture")
        raw = Path(args[idx + 1]).read_text(encoding="utf-8")
        data = json.loads(raw)
        result = classify_issues(
            data.get("issues") or [],
            data.get("discussions") or [],
            board,
            bool(data.get("truncated")),
        )
    else:
        issues, truncated = fetch_issues()
        result = classify_issues(issues, [], board, truncated)
    print(json.dumps(result, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
