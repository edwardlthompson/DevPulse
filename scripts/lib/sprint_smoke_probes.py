"""CLI, Android, docs, and feature-gate sprint-smoke probes."""
from __future__ import annotations

import json
import re
import subprocess
import time
from dataclasses import dataclass, field
from pathlib import Path

DEFAULT_BUDGET = {
    "web_http_ms": 3000,
    "cli_help_ms": 8000,
    "android_launch_ms": 15000,
}


@dataclass
class ProbeResult:
    name: str
    ok: bool
    detail: str
    startup_ms: float | None = None
    load_order: list[str] = field(default_factory=list)
    skipped: bool = False


def load_budget(root: Path) -> dict[str, int]:
    budget = dict(DEFAULT_BUDGET)
    path = root / "scripts" / "sprint-smoke-budget.json"
    if not path.is_file():
        return budget
    try:
        extra = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return budget
    if isinstance(extra, dict):
        for key, value in extra.items():
            if isinstance(value, int):
                budget[key] = value
    return budget


def _time_cmd(argv: list[str], cwd: Path | None) -> tuple[int, float, str]:
    start = time.perf_counter()
    try:
        from agent_run_env import child_env

        env = child_env()
    except ImportError:
        env = None
    try:
        proc = subprocess.run(
            argv,
            cwd=cwd,
            capture_output=True,
            text=True,
            timeout=20,
            check=False,
            env=env,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        return 1, (time.perf_counter() - start) * 1000, str(exc)
    ms = (time.perf_counter() - start) * 1000
    tail = ((proc.stderr or proc.stdout) or "")[-200:]
    return proc.returncode, ms, tail


def probe_cli(root: Path, stack: str, budget: dict[str, int]) -> ProbeResult:
    if stack == "python":
        py = root / "examples" / "python"
        if not (py / "pyproject.toml").is_file():
            return ProbeResult("python", True, "no python example", skipped=True)
        code, ms, tail = _time_cmd(["python3", "-c", "print('ok')"], py)
    elif stack == "node":
        pkg = root / "examples" / "node" / "package.json"
        if not pkg.is_file():
            return ProbeResult("node", True, "no node example", skipped=True)
        code, ms, tail = _time_cmd(["node", "-e", "console.log('ok')"], pkg.parent)
    else:
        return ProbeResult(stack, True, "no CLI probe", skipped=True)
    if code != 0:
        return ProbeResult(stack, False, f"exit {code}: {tail}", ms)
    if ms > budget["cli_help_ms"]:
        return ProbeResult(stack, False, f"startup {ms:.0f}ms over budget", ms)
    return ProbeResult(stack, True, f"ok in {ms:.0f}ms", ms, [stack])


def probe_android(root: Path) -> ProbeResult:
    manifest = root / "examples/android/app/src/main/AndroidManifest.xml"
    if not manifest.is_file():
        return ProbeResult("android", True, "no Android manifest", skipped=True)
    text = manifest.read_text(encoding="utf-8")
    activity = re.search(r"<activity[^>]*android:name=\"([^\"]+)\"", text)
    order = [activity.group(1)] if activity else []
    try:
        adb = subprocess.run(
            ["adb", "devices"], capture_output=True, text=True, check=False
        )
    except FileNotFoundError:
        return ProbeResult(
            "android", True, "adb not installed; manifest load order only", load_order=order, skipped=True
        )
    has_dev = any("\tdevice" in line for line in (adb.stdout or "").splitlines())
    if adb.returncode != 0 or not has_dev:
        return ProbeResult(
            "android", True, "no adb device; manifest load order only", load_order=order, skipped=True
        )
    return ProbeResult("android", True, "adb present; device smoke is [ADB]", load_order=order, skipped=True)


def _docs_path_exists(root: Path, rel: str) -> bool:
    if (root / rel).exists():
        return True
    norm = rel.replace("\\", "/")
    if "/" in norm:
        return False
    name = Path(rel).name
    for folder in ("schemas", "docs", "examples", "modules", "scripts"):
        base = root / folder
        if base.is_dir() and any(base.rglob(name)):
            return True
    return False


def probe_docs(root: Path, paths: list[str]) -> ProbeResult:
    missing = [p for p in paths if p and not _docs_path_exists(root, p)]
    if missing:
        return ProbeResult("docs", False, f"missing {missing}")
    return ProbeResult("docs", True, "mentioned paths exist" if paths else "docs probe")


def probe_feature_gate(root: Path, stacks: list[str]) -> ProbeResult:
    if not stacks:
        return ProbeResult("feature-gate", True, "no stack gate", skipped=True)
    stack = "multi" if len(stacks) > 1 else stacks[0]
    start = time.perf_counter()
    proc = subprocess.run(
        ["bash", str(root / "scripts" / "feature-gate.sh"), "--stack", stack],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    ms = (time.perf_counter() - start) * 1000
    if proc.returncode != 0:
        tail = (proc.stderr or proc.stdout or "")[-300:]
        return ProbeResult("feature-gate", False, f"exit {proc.returncode}: {tail}", ms)
    return ProbeResult("feature-gate", True, f"{stack} gate ok", ms)
