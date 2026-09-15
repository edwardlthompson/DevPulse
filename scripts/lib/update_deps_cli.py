"""CLI runner for local-first dependency updates."""
from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path

from gradle_apply import HINT as GRADLE_PIN_HINT, apply_env_pins
from update_deps import (
    audit_jobs,
    discover_langs,
    gradle_fallback_message,
    kotlin_guard_error,
    timeout_seconds,
    upd_argv,
)


def run_cmd(argv: list[str], timeout: int, cwd: Path | None) -> int:
    from agent_run_env import child_env

    env = child_env()
    resolved = shutil.which(argv[0], path=env.get("PATH"))
    cmd = [resolved, *argv[1:]] if resolved else argv
    try:
        proc = subprocess.run(cmd, cwd=cwd, timeout=timeout, check=False, env=env)
    except subprocess.TimeoutExpired:
        print(f"FAIL: timeout after {timeout}s: {' '.join(argv)}", file=sys.stderr)
        return 1
    except FileNotFoundError:
        print(f"WARN: command not found: {argv[0]}", file=sys.stderr)
        return 127
    return proc.returncode


def parse_mode(argv: list[str] | None = None) -> str:
    parser = argparse.ArgumentParser(description="Local-first dependency updater")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--apply", action="store_true")
    group.add_argument("--audit", action="store_true")
    group.add_argument("--dry-run", action="store_true")
    parsed = parser.parse_args(argv)
    if parsed.apply:
        return "apply"
    if parsed.audit:
        return "audit"
    return "dry-run"


def main(argv: list[str] | None = None, root: Path | None = None) -> int:
    root = root or Path.cwd()
    mode = parse_mode(argv)
    timeout = timeout_seconds()
    hint = gradle_fallback_message(root)
    if hint:
        print(hint, file=sys.stderr, flush=True)
        print(GRADLE_PIN_HINT, file=sys.stderr, flush=True)
    pins = os.environ.get("UPDATE_GRADLE_PINS", "")
    if pins and mode == "dry-run":
        for line in apply_env_pins(root, pins, write=False):
            print(f"Gradle pin (dry-run): {line}", file=sys.stderr, flush=True)
    if mode == "audit":
        jobs = audit_jobs(root)
        if not jobs:
            print("FAIL: no local scanners (need uvx, npm, or uv)", file=sys.stderr)
            return 1
        from concurrent.futures import ThreadPoolExecutor, as_completed

        workers = min(3, len(jobs))
        errors = 0

        def _one(item: tuple[list[str], Path | None]) -> tuple[str, int]:
            cmd, cwd = item
            label = " ".join(cmd)
            print(f"=== audit: {label} ===", flush=True)
            return label, run_cmd(cmd, timeout, cwd)

        with ThreadPoolExecutor(max_workers=workers) as pool:
            futs = [pool.submit(_one, job) for job in jobs]
            for fut in as_completed(futs):
                label, code = fut.result()
                if code != 0:
                    print(f"FAIL audit: {label} (exit {code})", file=sys.stderr)
                    errors += 1
        return 1 if errors else 0
    langs = discover_langs(root)
    if not langs:
        print("WARN: no updatable ecosystems found")
        return 1 if mode == "apply" else 0
    if not shutil.which("uvx"):
        print("WARN: uvx not on PATH; skip upd", file=sys.stderr)
        return 1 if mode == "apply" else 0
    cmd = upd_argv(mode, langs)
    print(f"=== {' '.join(cmd)} ===", file=sys.stderr, flush=True)
    code = run_cmd(cmd, timeout, root)
    if mode == "dry-run":
        from upd_canvas import write_status

        dest = write_status(root)
        print(f"Wrote {dest}", file=sys.stderr, flush=True)
        return 0 if code in (0, 1) else 1
    if code != 0:
        return 1
    if mode != "apply":
        return 0
    err = kotlin_guard_error(root)
    if err:
        print(f"FAIL: {err}", file=sys.stderr)
        return 1
    if pins:
        for line in apply_env_pins(root, pins, write=True):
            print(f"Gradle pin: {line}", file=sys.stderr, flush=True)
        err = kotlin_guard_error(root)
        if err:
            print(f"FAIL: {err}", file=sys.stderr)
            return 1
    fmt = root / "scripts" / "check-workflow-action-ref-format.sh"
    if fmt.is_file() and "actions" in langs:
        if run_cmd(["bash", str(fmt)], timeout, root) != 0:
            return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
