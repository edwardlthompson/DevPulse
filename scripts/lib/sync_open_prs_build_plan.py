"""Sync open Dependabot and Release Please PRs into a managed BUILD_PLAN block."""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path
from typing import Any

from build_plan_tally import apply_tally
from sync_open_prs_fetch import fetch_open_prs, resolve_gh
from sync_open_prs_render import (
    BEGIN,
    EMPTY_NOTE,
    END,
    classify_pr,
    extract_inner,
    relevant_prs,
    render_inner,
    replace_inner,
)

# Re-export for callers / tests
__all__ = [
    "BEGIN",
    "EMPTY_NOTE",
    "END",
    "classify_pr",
    "extract_inner",
    "fetch_open_prs",
    "relevant_prs",
    "render_inner",
    "resolve_gh",
    "sync_file",
    "sync_text",
]


def sync_text(text: str, prs: list[dict[str, Any]]) -> str:
    return apply_tally(replace_inner(text, render_inner(relevant_prs(prs))))


def sync_file(
    path: Path,
    prs: list[dict[str, Any]] | None,
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
        print(f"FAIL: {path} missing open-prs-sync markers", file=sys.stderr)
        return 1
    if prs is None:
        try:
            prs = fetch_open_prs(root)
        except (OSError, subprocess.TimeoutExpired, RuntimeError, json.JSONDecodeError) as exc:
            msg = f"gh unavailable or failed: {exc}"
            if check:
                print(f"FAIL: {msg}", file=sys.stderr)
                return 1
            print(f"SKIP: {msg}", file=sys.stderr)
            return 0
    try:
        updated = sync_text(raw, prs)
    except ValueError as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        return 1
    if raw == updated:
        print(f"OK: {path.name} open-prs-sync")
        return 0
    if check:
        print(f"FAIL: {path} open-prs-sync stale; run sync-open-prs-build-plan --apply", file=sys.stderr)
        return 1
    if apply:
        path.write_text(updated, encoding="utf-8", newline="\n")
        print(f"Updated {path}")
        return 0
    print(f"DRY-RUN: {path.name} would update open-prs-sync block")
    print(extract_inner(updated) or "")
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".")
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--prs-json", help="Path to fixture JSON (skip gh). For tests.")
    parser.add_argument("paths", nargs="*")
    args = parser.parse_args(argv)
    if args.apply and args.check:
        print("FAIL: use --apply or --check, not both", file=sys.stderr)
        return 1
    root = Path(args.root).resolve()
    paths = [Path(p) for p in args.paths] if args.paths else [root / "BUILD_PLAN.md"]
    prs: list[dict[str, Any]] | None = None
    if args.prs_json:
        prs = json.loads(Path(args.prs_json).read_text(encoding="utf-8"))
        if not isinstance(prs, list):
            print("FAIL: --prs-json must be a JSON list", file=sys.stderr)
            return 1
    code = 0
    for path in paths:
        target = path if path.is_absolute() else root / path
        code = max(code, sync_file(target, prs, apply=args.apply, check=args.check, root=root))
    return code


if __name__ == "__main__":
    sys.exit(main())
