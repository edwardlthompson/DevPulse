"""CI anti-amnesia: AGENT.md brief must stay on the BUILD_PLAN Product block."""
from __future__ import annotations

import sys
from pathlib import Path

from agent_brief import EMPTY_NOTE, extract_inner, parse_brief
from build_sprint_model import is_template_repo

EXAMPLE = "AGENT.md.example"


def check(root: Path) -> list[str]:
    errors: list[str] = []
    example = root / EXAMPLE
    if not example.is_file():
        errors.append(f"MISSING: {EXAMPLE}")
    agent = root / "AGENT.md"
    template = is_template_repo(root)
    if template and not agent.is_file():
        return errors
    if not agent.is_file():
        errors.append("MISSING: AGENT.md (copy AGENT.md.example before init)")
        return errors
    brief = parse_brief(agent.read_text(encoding="utf-8"))
    one = str(brief.get("one_liner") or "").strip()
    keys = [str(k) for k in (brief.get("keywords") or []) if str(k).strip()]
    if not one:
        errors.append("AGENT.md missing agent-brief:one-liner")
        return errors
    plan = root / "BUILD_PLAN.md"
    if not plan.is_file():
        errors.append("MISSING: BUILD_PLAN.md")
        return errors
    inner = extract_inner(plan.read_text(encoding="utf-8"))
    if inner is None:
        errors.append("BUILD_PLAN.md missing product-brief-sync markers")
        return errors
    hay = inner.lower()
    if one.lower() not in hay:
        errors.append("BUILD_PLAN Product block missing one-liner from AGENT.md")
    for key in keys:
        if key not in hay:
            errors.append(f"BUILD_PLAN Product block missing keyword {key!r}")
    if inner.strip() == EMPTY_NOTE and keys:
        errors.append("BUILD_PLAN Product block is empty while AGENT.md has a brief")
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    root = Path(args[0]).resolve() if args else Path.cwd()
    errors = check(root)
    if errors:
        for item in errors:
            print(f"FAIL: {item}", file=sys.stderr)
        return 1
    print("OK: agent-brief anti-amnesia")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
