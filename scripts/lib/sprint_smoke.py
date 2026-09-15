#!/usr/bin/env python3
"""Sprint wrap smoke: prove every ✅ row, record startup and load order."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from sprint_smoke_map import backtick_paths, infer_probes
from sprint_smoke_parse import find_sprint, parse_sprints
from sprint_smoke_probes import (
    ProbeResult,
    load_budget,
    probe_android,
    probe_cli,
    probe_docs,
    probe_feature_gate,
)
from sprint_smoke_web import probe_web


def _write_report(root: Path, payload: dict) -> None:
    out = root / ".cursor" / "sprint-smoke.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def run(root: Path, args: argparse.Namespace) -> int:
    text = (root / "BUILD_PLAN.md").read_text(encoding="utf-8")
    sprints = parse_sprints(text)
    sprint = find_sprint(sprints, args.sprint)
    if sprint is None:
        titles = ", ".join(s.title for s in sprints) or "(none)"
        wanted = args.sprint or "(auto)"
        print(
            f"FAIL: no matching sprint for {wanted!r}; known ### headers: {titles}",
            file=sys.stderr,
        )
        return 2
    if args.if_complete and not sprint.complete:
        print(f"SKIP: {sprint.title} still has open AGENT/AUTO rows")
        return 0
    if args.require and not sprint.complete:
        open_n = len(sprint.open_agent_auto)
        print(f"FAIL: {sprint.title} has {open_n} open AGENT/AUTO row(s)", file=sys.stderr)
        return 2
    items = sprint.checked if sprint.complete else sprint.agent_auto
    if args.dry_run:
        mapping = {f"{i.number}": infer_probes(i.task) for i in sprint.agent_auto}
        print(json.dumps({"sprint": sprint.title, "complete": sprint.complete, "probes": mapping}, indent=2))
        return 0
    budget = load_budget(root)
    results = [
        probe_web(root, budget),
        probe_android(root),
        probe_cli(root, "python", budget),
        probe_cli(root, "node", budget),
    ]
    if args.with_feature_gate:
        stacks = sorted({p for i in items for p in infer_probes(i.task) if p in {"web", "android", "node", "python"}})
        results.append(probe_feature_gate(root, stacks))
    by_name = {r.name: r for r in results}
    item_rows = []
    failed = [r for r in results if not r.ok]
    for item in items:
        needed = infer_probes(item.task)
        if "docs" in needed:
            docs = probe_docs(root, backtick_paths(item.task))
            by_name["docs"] = docs
            results.append(docs)
            if not docs.ok:
                failed.append(docs)
        for name in needed:
            if name not in by_name:
                by_name[name] = ProbeResult(name, True, "no runtime probe", skipped=True)
        bad = [n for n in needed if not by_name[n].ok]
        ok = not bad
        item_rows.append({"id": item.number, "task": item.task, "probes": needed, "ok": ok})
        if not ok:
            failed.append(ProbeResult("item", False, f"{item.number} failed {bad}"))
    report = {
        "sprint": sprint.title,
        "complete": sprint.complete,
        "ok": not failed,
        "startup": {r.name: r.startup_ms for r in results if r.startup_ms is not None},
        "load_order": {r.name: r.load_order for r in results if r.load_order},
        "probes": [{"name": r.name, "ok": r.ok, "detail": r.detail, "skipped": r.skipped} for r in results],
        "items": item_rows,
    }
    _write_report(root, report)
    print(json.dumps(report, indent=2, ensure_ascii=False))
    if failed:
        print("FAIL: sprint smoke — errors, crashes, or budget", file=sys.stderr)
        return 1
    print(f"PASS: {sprint.title} smoked ({len(item_rows)} items)")
    print(
        "REMINDER: Update AGENT_MEMORY.md at this milestone boundary "
        "(Persistent Context + retrospective only; see AGENTS.md Session Protocol)."
    )
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Smoke every ✅ BUILD_PLAN row in a sprint")
    parser.add_argument("--root", default=".")
    parser.add_argument("--sprint", default=None, help="M50, M51, or Sprint 2")
    parser.add_argument("--require", action="store_true", help="Fail if the sprint is incomplete")
    parser.add_argument("--if-complete", action="store_true", help="Skip when AGENT rows remain")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--with-feature-gate", action="store_true")
    args = parser.parse_args()
    return run(Path(args.root).resolve(), args)


if __name__ == "__main__":
    sys.exit(main())
