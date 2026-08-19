#!/usr/bin/env python3
"""CLI for parallel scope checks and dispatch manifests."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "lib"))

from parallel_scope import (  # noqa: E402
    build_manifest,
    check_build_plan_parallel,
    find_overlaps,
    parse_parallel_rows,
    agent_rows,
)


def cmd_check_overlap(build_plan: Path) -> int:
    text = build_plan.read_text(encoding="utf-8")
    rows = agent_rows(parse_parallel_rows(text))
    scopes = [r.scope for r in rows]
    errors = find_overlaps(scopes)
    if errors:
        print("Parallel scope collisions detected:")
        for e in errors:
            print(f"  {e}")
        return 1
    print("No parallel scope overlaps detected")
    return 0


def cmd_check_build_plan(build_plan: Path, min_agents: int) -> int:
    ok, errors = check_build_plan_parallel(build_plan, min_agents=min_agents)
    if ok:
        print("BUILD_PLAN parallel structure check passed")
        return 0
    print("BUILD_PLAN parallel structure check failed:")
    for e in errors:
        print(f"  {e}")
    return 1


def cmd_manifest(args: argparse.Namespace) -> int:
    build_plan = Path(args.build_plan)
    root = build_plan.parent
    manifest = build_manifest(
        root,
        build_plan,
        stack=args.stack,
        feature=args.feature,
        require_sequential_clear=args.require_sequential_clear,
        suggest=args.suggest,
    )
    if args.json:
        print(json.dumps(manifest, indent=2))
    else:
        n = manifest["agent_count"]
        if manifest["agents"]:
            parts = ", ".join(
                f"{a['id']}: {a['task']}" for a in manifest["agents"]
            )
            print(f"Parallel dispatch: {n} agent(s) ({parts})")
        else:
            print("Parallel dispatch: 0 agents")
        if manifest["blockers"]:
            print("Blockers:")
            for b in manifest["blockers"]:
                print(f"  {b}")
        if args.suggest and manifest["suggestions"]:
            print("Suggestions:")
            for s in manifest["suggestions"]:
                print(f"  - {s['task']}: `{s['scope']}`")
        if manifest.get("split_hint"):
            print(f"Split hint: {manifest['split_hint']}")
    if manifest["blockers"] and args.require_sequential_clear:
        return 1
    if args.require_sequential_clear and manifest["blockers"]:
        return 1
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Parallel scope utilities")
    parser.add_argument("--build-plan", default=str(ROOT / "BUILD_PLAN.md"))
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("check-overlap", help="Detect overlapping Parallel scopes")

    bp = sub.add_parser("check-build-plan", help="Validate sprint Parallel tables")
    bp.add_argument("--min-agents", type=int, default=2)

    mf = sub.add_parser("manifest", help="Build dispatch manifest")
    mf.add_argument("--json", action="store_true")
    mf.add_argument("--suggest", action="store_true")
    mf.add_argument("--require-sequential-clear", action="store_true")
    mf.add_argument("--stack", default=None)
    mf.add_argument("--feature", default=None)

    args = parser.parse_args()
    build_plan = Path(args.build_plan)

    if args.command == "check-overlap":
        return cmd_check_overlap(build_plan)
    if args.command == "check-build-plan":
        return cmd_check_build_plan(build_plan, args.min_agents)
    if args.command == "manifest":
        return cmd_manifest(args)
    return 1


if __name__ == "__main__":
    sys.exit(main())
