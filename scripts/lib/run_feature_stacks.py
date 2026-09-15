"""Run feature-gate.sh per stack in RAM-capped waves. Parent-only JSON."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path

from local_resources import (
    InvalidJobs,
    discover_stacks,
    recommended_stack_slots,
    schedule_waves,
)

ROOT = Path(__file__).resolve().parent.parent.parent
SCRIPT = ROOT / "scripts" / "feature-gate.sh"


def _bash() -> str:
    if os.name == "nt":
        for base in (
            os.environ.get("ProgramFiles", r"C:\Program Files"),
            os.environ.get("ProgramFiles(x86)", r"C:\Program Files (x86)"),
        ):
            candidate = Path(base) / "Git" / "bin" / "bash.exe"
            if candidate.is_file():
                return str(candidate)
        which = shutil.which("bash")
        if which and "System32" not in which.replace("/", "\\"):
            return which
        return "bash"
    return shutil.which("bash") or "bash"


def select_stacks(discovered: list[str], only_csv: str | None = None) -> list[str]:
    raw = (only_csv if only_csv is not None else os.environ.get("FEATURE_GATE_ONLY", "")).strip()
    if not raw:
        return discovered
    wanted = {s.strip() for s in raw.split(",") if s.strip()}
    return [s for s in discovered if s in wanted]


def run_child(stack: str, extra: list[str], env: dict[str, str]) -> tuple[str, int, str]:
    cmd = [_bash(), str(SCRIPT.relative_to(ROOT).as_posix()), "--stack", stack, "--skip-preamble", *extra]
    child_env = dict(env)
    child_env["FEATURE_GATE_CHILD"] = "1"
    proc = subprocess.run(
        cmd,
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
        env=child_env,
    )
    out = (proc.stdout or "") + (proc.stderr or "")
    return stack, proc.returncode, out


def main(argv: list[str] | None = None) -> int:
    extra = list(argv if argv is not None else sys.argv[1:])
    extra = [a for a in extra if a not in {"--json", "--strict"}]
    try:
        slots = recommended_stack_slots()
    except InvalidJobs as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        return 2
    stacks = select_stacks(discover_stacks(ROOT))
    if not stacks:
        print("WARN: no stack markers found", file=sys.stderr)
        return 0
    waves = schedule_waves(stacks, slots)
    only = os.environ.get("FEATURE_GATE_ONLY", "").strip()
    scoped = f" only={only}" if only else ""
    print(f"Parallel stacks: {stacks} slots={slots} waves={waves}{scoped}", flush=True)
    failed: list[str] = []
    logs: dict[str, str] = {}
    env = os.environ.copy()
    for wave in waves:
        results: list[tuple[str, int, str]] = []
        if len(wave) == 1:
            results.append(run_child(wave[0], extra, env))
        else:
            from concurrent.futures import ThreadPoolExecutor

            with ThreadPoolExecutor(max_workers=len(wave)) as pool:
                futs = [pool.submit(run_child, name, extra, env) for name in wave]
                results = [fut.result() for fut in futs]
        for name, code, out in results:
            logs[name] = out
            if code != 0:
                failed.append(f"{name}:{code}")
    for name in stacks:
        body = logs.get(name, "")
        if body.strip():
            print(f"----- stack {name} -----")
            sys.stdout.write(body)
            if not body.endswith("\n"):
                print()
    if failed:
        print("FAIL stacks: " + ", ".join(failed), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
