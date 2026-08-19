#!/usr/bin/env python3
"""Run independent bash check scripts in parallel (local CPU). Fail if any fail."""
from __future__ import annotations

import os
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
SCRIPTS = ROOT / "scripts"


def worker_count() -> int:
    env = os.environ.get("BOOTSTRAP_CHECK_JOBS", "").strip()
    if env.isdigit() and int(env) > 0:
        return int(env)
    return max(1, os.cpu_count() or 2)


def run_one(script_name: str) -> tuple[str, int, str]:
    script = SCRIPTS / script_name
    if not script.is_file():
        return script_name, 127, f"missing script: {script}"
    # Prefer Git Bash on Windows via agent-run pattern — invoke through bash when needed
    if os.name == "nt":
        bash_candidates = [
            Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Git" / "bin" / "bash.exe",
            Path(os.environ.get("ProgramFiles(x86)", r"C:\Program Files (x86)"))
            / "Git"
            / "bin"
            / "bash.exe",
        ]
        bash = next((str(p) for p in bash_candidates if p.is_file()), None)
        if bash:
            cmd = [bash, str(script.relative_to(ROOT).as_posix())]
        else:
            cmd = ["bash", str(script)]
    else:
        cmd = ["bash", str(script)]
    proc = subprocess.run(
        cmd,
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    out = (proc.stdout or "") + (proc.stderr or "")
    return script_name, proc.returncode, out


def main(argv: list[str]) -> int:
    if not argv:
        print("Usage: run_checks_parallel.py <script.sh> [more.sh...]", file=sys.stderr)
        return 2
    jobs = worker_count()
    print(f"Parallel checks: {len(argv)} scripts, jobs={jobs}", flush=True)
    failed = 0
    with ThreadPoolExecutor(max_workers=jobs) as pool:
        futures = {pool.submit(run_one, name): name for name in argv}
        for fut in as_completed(futures):
            name, code, out = fut.result()
            if out.strip():
                sys.stdout.write(out)
                if not out.endswith("\n"):
                    sys.stdout.write("\n")
            if code != 0:
                print(f"FAIL: {name} (exit {code})", file=sys.stderr)
                failed += 1
            else:
                print(f"OK: {name}", flush=True)
    if failed:
        print(f"{failed} parallel check(s) failed", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
