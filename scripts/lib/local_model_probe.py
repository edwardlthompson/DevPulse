"""GPU/RAM/Ollama probes for local model recommendations."""
from __future__ import annotations

import json
import platform
import re
import shutil
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

from local_resources import GIB, OLLAMA_URL, ollama_up

PROBE_TIMEOUT = 5.0


def _run(cmd: list[str]) -> str | None:
    if not shutil.which(cmd[0]):
        return None
    try:
        proc = subprocess.run(
            cmd, capture_output=True, text=True, timeout=PROBE_TIMEOUT, check=False
        )
    except (OSError, subprocess.TimeoutExpired):
        return None
    return (proc.stdout or "").strip() if proc.returncode == 0 else None


def detect_nvidia() -> tuple[int | None, str | None]:
    out = _run(["nvidia-smi", "--query-gpu=memory.total,name", "--format=csv,noheader,nounits"])
    if not out:
        return None, None
    parts = [p.strip() for p in out.splitlines()[0].split(",")]
    try:
        return int(float(parts[0])), (parts[1] if len(parts) > 1 else "NVIDIA")
    except (ValueError, IndexError):
        return None, None


def detect_amd() -> tuple[int | None, str | None]:
    out = _run(["rocm-smi", "--showmeminfo", "vram"])
    if not out:
        return None, None
    m = re.search(r"(\d+)\s*MiB", out, re.I) or re.search(r"Total\s*Memory[^0-9]*(\d+)", out, re.I)
    return (int(m.group(1)), "AMD") if m else (None, None)


def detect_apple_unified_mib() -> tuple[int | None, str | None]:
    if platform.system() != "Darwin":
        return None, None
    out = _run(["sysctl", "-n", "hw.memsize"])
    return (int(out) // (1024 * 1024), "Apple Silicon") if out and out.isdigit() else (None, None)


def ram_total_gb() -> int | None:
    try:
        if sys.platform == "win32":
            import ctypes

            class _MX(ctypes.Structure):
                _fields_ = [
                    ("dwLength", ctypes.c_ulong),
                    ("dwMemoryLoad", ctypes.c_ulong),
                    ("ullTotalPhys", ctypes.c_ulonglong),
                    ("ullAvailPhys", ctypes.c_ulonglong),
                    ("ullTotalPageFile", ctypes.c_ulonglong),
                    ("ullAvailPageFile", ctypes.c_ulonglong),
                    ("ullTotalVirtual", ctypes.c_ulonglong),
                    ("ullAvailVirtual", ctypes.c_ulonglong),
                    ("ullAvailExtendedVirtual", ctypes.c_ulonglong),
                ]

            stat = _MX()
            stat.dwLength = ctypes.sizeof(_MX)
            if ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(stat)):
                return max(1, int(stat.ullTotalPhys) // GIB)
            return None
        meminfo = Path("/proc/meminfo")
        if meminfo.is_file():
            for line in meminfo.read_text(encoding="utf-8", errors="replace").splitlines():
                if line.startswith("MemTotal:"):
                    return max(1, int(line.split()[1]) * 1024 // GIB)
        if platform.system() == "Darwin":
            out = _run(["sysctl", "-n", "hw.memsize"])
            if out and out.isdigit():
                return max(1, int(out) // GIB)
    except (OSError, ValueError, AttributeError):
        return None
    return None


def ollama_tags() -> set[str]:
    if not ollama_up():
        return set()
    try:
        opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
        with opener.open(OLLAMA_URL, timeout=1.0) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except (urllib.error.URLError, TimeoutError, OSError, ValueError, json.JSONDecodeError):
        return set()
    names: set[str] = set()
    for item in data.get("models") or []:
        name = item.get("name") if isinstance(item, dict) else None
        if isinstance(name, str) and name:
            names.update({name, name.split(":")[0]})
    return names


def probe_gpu() -> tuple[int | None, str | None, list[str]]:
    warnings: list[str] = []
    for fn in (detect_nvidia, detect_amd):
        mib, name = fn()
        if mib is not None:
            return mib, name, warnings
    mib, name = detect_apple_unified_mib()
    if mib is not None:
        warnings.append("Apple unified memory treated as VRAM budget")
        return mib, name, warnings
    warnings.append("No GPU probe (nvidia-smi/rocm-smi/Apple); entry tier")
    return None, None, warnings
