"""CPU/RAM job caps and a 1s localhost Ollama probe. Never raises to callers."""
from __future__ import annotations

import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

GIB = 1024**3
OLLAMA_URL = "http://127.0.0.1:11434/api/tags"
SINGLE_STACKS = ("android", "blender", "go", "lightroom", "node", "python", "rust", "web")
MARKERS = {
    "android": "examples/android/gradlew",
    "blender": "examples/blender/blender.toml",
    "go": "examples/go/go.mod",
    "lightroom": "examples/lightroom/Info.lua",
    "node": "examples/node/package.json",
    "python": "examples/python/pyproject.toml",
    "rust": "examples/rust/Cargo.toml",
    "web": "examples/web/package.json",
}


class InvalidJobs(ValueError):
    """Env job count present but not a positive integer."""


def cpu_count() -> int:
    return max(1, os.cpu_count() or 1)


def in_ci() -> bool:
    vals = {os.environ.get("GITHUB_ACTIONS", "").lower(), os.environ.get("CI", "").lower()}
    return bool(vals & {"1", "true"})


def env_jobs(name: str) -> int | None:
    raw = os.environ.get(name, "").strip()
    if raw == "":
        return None
    if not raw.isdigit() or int(raw) <= 0:
        raise InvalidJobs(f"{name} must be a positive int, got {raw!r}")
    return int(raw)


def ram_available_bytes() -> int | None:
    try:
        if sys.platform == "win32":
            return _ram_windows()
        meminfo = Path("/proc/meminfo")
        if meminfo.is_file():
            return _ram_linux(meminfo.read_text(encoding="utf-8", errors="replace"))
    except (OSError, ValueError, AttributeError):
        return None
    return None


def _ram_linux(text: str) -> int | None:
    for line in text.splitlines():
        if line.startswith("MemAvailable:"):
            return int(line.split()[1]) * 1024
    return None


def _ram_windows() -> int | None:
    import ctypes

    class MEMORYSTATUSEX(ctypes.Structure):
        _fields_ = [
            ("dwLength", ctypes.c_ulong), ("dwMemoryLoad", ctypes.c_ulong),
            ("ullTotalPhys", ctypes.c_ulonglong), ("ullAvailPhys", ctypes.c_ulonglong),
            ("ullTotalPageFile", ctypes.c_ulonglong), ("ullAvailPageFile", ctypes.c_ulonglong),
            ("ullTotalVirtual", ctypes.c_ulonglong), ("ullAvailVirtual", ctypes.c_ulonglong),
            ("ullAvailExtendedVirtual", ctypes.c_ulonglong),
        ]

    stat = MEMORYSTATUSEX()
    stat.dwLength = ctypes.sizeof(MEMORYSTATUSEX)
    if not ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(stat)):
        return None
    return int(stat.ullAvailPhys)


def ram_gb_or_none() -> int | None:
    raw = ram_available_bytes()
    return None if raw is None else max(1, raw // GIB)


def recommended_check_jobs() -> int:
    env = env_jobs("BOOTSTRAP_CHECK_JOBS")
    if env is not None:
        return env
    cpu = cpu_count()
    gb = ram_gb_or_none()
    return cpu if gb is None else min(cpu, gb)


def recommended_stack_slots() -> int:
    env = env_jobs("FEATURE_GATE_JOBS")
    if env is not None:
        return env
    cpu = cpu_count()
    gb = ram_gb_or_none()
    slots = cpu if gb is None else min(cpu, max(1, gb // 3))
    if in_ci():
        slots = min(slots, 2)
    return max(1, slots)


def stack_weight(name: str, slots: int) -> int:
    return min(2, max(1, slots)) if name == "android" else 1


def schedule_waves(stacks: list[str], slots: int) -> list[list[str]]:
    remaining = list(stacks)
    waves: list[list[str]] = []
    cap = max(1, slots)
    while remaining:
        batch: list[str] = []
        used = 0
        leftover: list[str] = []
        for name in remaining:
            weight = stack_weight(name, cap)
            if used + weight <= cap:
                batch.append(name)
                used += weight
            else:
                leftover.append(name)
        if not batch:
            batch, leftover = [remaining[0]], remaining[1:]
        waves.append(batch)
        remaining = leftover
    return waves


def discover_stacks(root: Path) -> list[str]:
    return [name for name in SINGLE_STACKS if (root / MARKERS[name]).exists()]


def ollama_up(timeout: float = 1.0) -> bool:
    try:
        opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
        with opener.open(OLLAMA_URL, timeout=timeout):
            return True
    except (urllib.error.URLError, TimeoutError, OSError, ValueError):
        return False
