"""CLI for pre/post bootstrap hooks and adapter/checklist generation."""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

from agent_adapters import write_adapters
from bootstrap_engine import (
    apply_license,
    default_config,
    load_config,
    preflight,
    save_config,
    validate_config,
)
from bootstrap_post import create_welcome_issue, ensure_git_repo, install_deps, run_stack_tests
from project_checklist import write_checklist
from stamp_project import stamp_agents_md, stamp_first_30_days


def parse_args(argv: list[str]) -> argparse.Namespace:
    p = argparse.ArgumentParser(description="Bootstrap lifecycle hooks")
    p.add_argument("--root", type=Path, default=None)
    p.add_argument("--pre", action="store_true")
    p.add_argument("--post", action="store_true")
    p.add_argument("--sync-adapters", action="store_true")
    p.add_argument("--checklist", action="store_true")
    p.add_argument("--all", action="store_true")
    p.add_argument("--stack", default="")
    p.add_argument("--project-name", default="")
    p.add_argument("--purpose", default="")
    p.add_argument("--license", default="MIT")
    p.add_argument("--distribution-tier", default="foss")
    p.add_argument("--strict", action="store_true")
    p.add_argument("--skip-preflight", action="store_true")
    p.add_argument("--git-init", action="store_true")
    p.add_argument("--install-deps", action="store_true")
    p.add_argument("--run-tests", action="store_true")
    p.add_argument("--welcome-issue", action="store_true")
    return p.parse_args(argv)


def run(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    root = (args.root or Path(__file__).resolve().parent.parent.parent).resolve()
    existing = load_config(root) or {}
    stack = args.stack or str(existing.get("stack") or "none")
    name = args.project_name or str(existing.get("project_name") or "")
    purpose = args.purpose or str(existing.get("purpose") or "")
    license_id = args.license or str(existing.get("license") or "MIT")
    tier = args.distribution_tier or str(existing.get("distribution_tier") or "foss")
    do_pre = args.pre or args.all
    do_post = args.post or args.all
    do_adapters = args.sync_adapters or do_post
    do_list = args.checklist or do_post

    if do_pre and not args.skip_preflight:
        errors, warnings = preflight(stack, strict=args.strict)
        for w in warnings:
            print(f"WARN: {w}", file=sys.stderr)
        if errors:
            for e in errors:
                print(f"ERROR: {e}", file=sys.stderr)
            return 1
        print("Preflight passed (git + Python present).")

    if do_post:
        cfg = default_config(
            project_name=name or existing.get("project_name") or "project",
            purpose=purpose or existing.get("purpose") or "FOSS project",
            stack=stack,
            license_id=license_id,
            distribution_tier=tier,
        )
        if isinstance(existing.get("hooks"), dict):
            merged = dict(cfg.get("hooks") or {})
            merged.update(existing["hooks"])
            cfg["hooks"] = merged
        problems = validate_config(cfg)
        if problems:
            for e in problems:
                print(f"ERROR: {e}", file=sys.stderr)
            return 1
        save_config(root, cfg)
        apply_license(root, license_id)
        stamped = stamp_agents_md(root, name=cfg["project_name"], purpose=cfg["purpose"], stack=stack)
        if stamped:
            print(f"Stamped {stamped}")
        days = stamp_first_30_days(
            root, name=cfg["project_name"], purpose=cfg["purpose"], stack=stack
        )
        if days:
            print(f"Stamped {days}")
        print(f"Wrote {root / 'bootstrap.config.json'}")
        hooks = cfg.get("hooks") if isinstance(cfg.get("hooks"), dict) else {}
        try:
            if args.git_init or hooks.get("post_git_init"):
                print(ensure_git_repo(root))
            if args.install_deps or hooks.get("post_install_deps"):
                for note in install_deps(root, stack):
                    print(note)
            if args.run_tests or hooks.get("post_run_tests"):
                for note in run_stack_tests(root, stack):
                    print(note)
            if args.welcome_issue or hooks.get("post_welcome_issue"):
                print(create_welcome_issue(root))
        except RuntimeError as exc:
            print(f"ERROR: {exc}", file=sys.stderr)
            return 1

    if do_adapters:
        adapters = (existing.get("agent_adapters") if existing else None) or {}
        written = write_adapters(root, adapters if isinstance(adapters, dict) else None)
        for path in written:
            print(f"Wrote {path}")

    if do_list:
        path = write_checklist(
            root, project_name=name or "project", stack=stack, license_id=license_id
        )
        print(f"Wrote {path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(run())
