#!/usr/bin/env python3
"""INFO probe for local CPU/RAM/Ollama/emulator. Exit 0 unless misconfig."""
from __future__ import annotations

import json
import os
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from local_resources import (  # noqa: E402
    InvalidJobs,
    cpu_count,
    env_jobs,
    ollama_up,
    ram_gb_or_none,
    recommended_check_jobs,
    recommended_stack_slots,
)


def _adb() -> str | None:
    return shutil.which("adb")


def _sdk() -> str | None:
    for key in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        raw = os.environ.get(key, "").strip()
        if raw and Path(raw).is_dir():
            return raw
    return None


def _kvm() -> str:
    if sys.platform.startswith("linux"):
        return "yes" if Path("/dev/kvm").exists() else "no"
    if sys.platform == "darwin":
        return "hv"
    if sys.platform == "win32":
        return "unknown"
    return "unknown"


def _jobs_misconfig() -> str | None:
    for name in ("BOOTSTRAP_CHECK_JOBS", "FEATURE_GATE_JOBS"):
        try:
            env_jobs(name)
        except InvalidJobs as exc:
            return str(exc)
    return None


def _inotify_watches() -> int | None:
    if not sys.platform.startswith("linux"):
        return None
    path = Path("/proc/sys/fs/inotify/max_user_watches")
    try:
        return int(path.read_text(encoding="utf-8").strip())
    except (OSError, ValueError):
        return None


def _avd_present(sdk: str | None) -> bool:
    if not sdk:
        return False
    home = Path.home()
    candidates = [
        home / ".android" / "avd",
        Path(sdk) / ".android" / "avd",
    ]
    for base in candidates:
        if not base.is_dir():
            continue
        if any(base.glob("*.ini")) or any(base.iterdir()):
            return True
    return False


def main() -> int:
    bad = _jobs_misconfig()
    if bad:
        print(f"FAIL: {bad}", file=sys.stderr)
        return 2
    sel = ROOT / ".cursor" / "stack-selection.json"
    if sel.is_file():
        try:
            json.loads(sel.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            print(f"FAIL: corrupt stack-selection.json: {exc}", file=sys.stderr)
            return 2
    cpu = cpu_count()
    ram = ram_gb_or_none()
    try:
        jobs = recommended_check_jobs()
        slots = recommended_stack_slots()
    except InvalidJobs as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        return 2
    ollama = "up" if ollama_up() else "down"
    sdk = _sdk() or "none"
    adb = "yes" if _adb() else "no"
    kvm = _kvm()
    print(f"cpus={cpu} ram_gb={ram if ram is not None else 'unknown'} jobs={jobs} slots={slots}")
    print(f"ollama={ollama} emulator_gpu=unknown sdk={sdk} adb={adb} kvm={kvm}")
    if kvm == "no" and _avd_present(None if sdk == "none" else sdk):
        print(
            "WARN: AVD present but /dev/kvm missing — /emulator will skip; use a USB device "
            "(docs/LINUX_DEV.md)",
            file=sys.stderr,
        )
    watches = _inotify_watches()
    if watches is not None:
        print(f"inotify_max_user_watches={watches}")
        if watches < 100_000:
            print(
                "HINT: low inotify watches; see docs/LINUX_DEV.md "
                "(sudo sysctl -w fs.inotify.max_user_watches=524288)",
                file=sys.stderr,
            )
    elif sys.platform.startswith("linux"):
        print("HINT: Linux host — see docs/LINUX_DEV.md for SSD/caches/direnv/inotify", file=sys.stderr)
    mcp = ROOT / ".cursor" / "mcp.json"
    print(f"mcp.json={'yes' if mcp.is_file() else 'no (copy mcp.foss.example optional)'}")
    if ram is not None and ram < 16 and ollama == "up":
        print("WARN: RAM < 16G with Ollama up; skip /emulator or FEATURE_GATE_JOBS=1", file=sys.stderr)
    print("GPU: no CUDA in template; Ollama/emulator use host GPU when present")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
