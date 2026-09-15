"""Sync parent template gaps into a managed BUILD_PLAN block (plan-only)."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

from build_plan_tally import apply_tally
from build_sprint_model import is_template_repo
from sync_template_gaps_render import (
    BEGIN,
    EMPTY_NOTE,
    END,
    extract_inner,
    render_inner,
    replace_inner,
)
from template_gap import report as gap_report

__all__ = [
    "BEGIN",
    "EMPTY_NOTE",
    "END",
    "extract_inner",
    "render_inner",
    "replace_inner",
    "sync_file",
    "sync_text",
]


def sync_text(text: str, data: dict[str, Any], *, template_repo: bool) -> str:
    return apply_tally(replace_inner(text, render_inner(data, template_repo=template_repo)))


def sync_file(
    path: Path,
    data: dict[str, Any] | None,
    *,
    apply: bool,
    check: bool,
    root: Path,
) -> int:
    if not path.is_file():
        print(f"SKIP: {path} missing", file=sys.stderr)
        return 0
    raw = path.read_text(encoding="utf-8")
    if extract_inner(raw) is None:
        print(f"FAIL: {path} missing template-gaps-sync markers", file=sys.stderr)
        return 1
    template = is_template_repo(root)
    if data is None:
        data = gap_report(root) if not template else {}
    try:
        updated = sync_text(raw, data, template_repo=template)
    except ValueError as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        return 1
    if raw == updated:
        print(f"OK: {path.name} template-gaps-sync")
        return 0
    if check:
        print(
            f"FAIL: {path} template-gaps-sync stale; "
            "run sync-template-gaps-build-plan --apply",
            file=sys.stderr,
        )
        return 1
    if apply:
        path.write_text(updated, encoding="utf-8", newline="\n")
        print(f"Updated {path}")
        return 0
    print(f"DRY-RUN: {path.name} would update template-gaps-sync block")
    print(extract_inner(updated) or "")
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".")
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--report-json", help="Fixture gap report JSON (skip network).")
    parser.add_argument("paths", nargs="*")
    args = parser.parse_args(argv)
    if args.apply and args.check:
        print("FAIL: use --apply or --check, not both", file=sys.stderr)
        return 1
    root = Path(args.root).resolve()
    paths = [Path(p) for p in args.paths] if args.paths else [root / "BUILD_PLAN.md"]
    data: dict[str, Any] | None = None
    if args.report_json:
        loaded = json.loads(Path(args.report_json).read_text(encoding="utf-8"))
        if not isinstance(loaded, dict):
            print("FAIL: --report-json must be a JSON object", file=sys.stderr)
            return 1
        data = loaded
    code = 0
    for path in paths:
        target = path if path.is_absolute() else root / path
        code = max(code, sync_file(target, data, apply=args.apply, check=args.check, root=root))
    return code


if __name__ == "__main__":
    sys.exit(main())
