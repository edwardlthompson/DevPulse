"""Bootstrap manifest, preflight checks, and license application."""
from __future__ import annotations

import json
import os
import shutil
from pathlib import Path
from typing import Any

from bootstrap_defaults import SCHEMA_VERSION, default_config

LICENSES = ("MIT", "Apache-2.0", "GPL-3.0-or-later")
STACKS = ("web", "python", "android", "node", "multi", "none")
REQUIRED_TOOLS = ("git",)
OPTIONAL_TOOLS = ("docker",)
STACK_TOOLS: dict[str, tuple[str, ...]] = {
    "web": ("node", "npm"),
    "node": ("node", "npm"),
    "python": ("uv",),
    "android": ("java",),
    "multi": ("node", "npm"),
    "none": (),
}
CONFIG_NAME = "bootstrap.config.json"
EXAMPLE_NAME = "bootstrap.config.json.example"

__all__ = [
    "SCHEMA_VERSION",
    "LICENSES",
    "STACKS",
    "CONFIG_NAME",
    "android_sdk_present",
    "default_config",
    "validate_config",
    "load_config",
    "save_config",
    "tool_present",
    "python_present",
    "preflight",
    "apply_license",
]


def android_sdk_present(env: dict[str, str] | None = None, home: Path | None = None) -> bool:
    """True when ANDROID_HOME/SDK_ROOT or a common user SDK has platform-tools."""
    environ = env if env is not None else os.environ
    for key in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        raw = (environ.get(key) or "").strip()
        if raw and (Path(raw).expanduser() / "platform-tools").is_dir():
            return True
    base = home if home is not None else Path.home()
    for candidate in (base / "Android" / "Sdk", base / ".local" / "android"):
        if (candidate / "platform-tools").is_dir():
            return True
    return False


def validate_config(cfg: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    if not isinstance(cfg, dict) or not cfg:
        return ["bootstrap config is empty or not an object"]
    stack = str(cfg.get("stack") or "")
    if stack not in STACKS:
        errors.append(f"invalid stack: {stack!r}")
    license_id = str(cfg.get("license") or "")
    if license_id not in LICENSES:
        errors.append(f"invalid license: {license_id!r}")
    if not str(cfg.get("project_name") or "").strip():
        errors.append("project_name is required")
    if not str(cfg.get("purpose") or "").strip():
        errors.append("purpose is required")
    return errors


def load_config(root: Path) -> dict[str, Any] | None:
    path = root / CONFIG_NAME
    if not path.is_file():
        example = root / EXAMPLE_NAME
        path = example if example.is_file() else path
    if not path.is_file():
        return None
    data = json.loads(path.read_text(encoding="utf-8"))
    return data if isinstance(data, dict) else None


def save_config(root: Path, cfg: dict[str, Any]) -> Path:
    path = root / CONFIG_NAME
    path.write_text(json.dumps(cfg, indent=2) + "\n", encoding="utf-8")
    return path


def tool_present(name: str) -> bool:
    return shutil.which(name) is not None


def python_present() -> bool:
    if tool_present("python3") or tool_present("python"):
        return True
    return tool_present("py")


def preflight(stack: str, *, strict: bool = False) -> tuple[list[str], list[str]]:
    errors: list[str] = []
    warnings: list[str] = []
    if not tool_present("git"):
        errors.append("git is required. Install Git and retry init.")
    if not python_present():
        errors.append("Python 3 is required. Install python3 (or py -3 on Windows).")
    for name in STACK_TOOLS.get(stack, ()):
        if not tool_present(name):
            msg = f"{name} not found (recommended for stack {stack})"
            (errors if strict else warnings).append(msg)
    if stack in ("android", "multi"):
        skip = (os.environ.get("SKIP_ANDROID_SDK") or "").strip().lower() in (
            "1",
            "true",
            "yes",
        )
        if not android_sdk_present():
            msg = (
                "Android SDK not found (set ANDROID_HOME with platform-tools, "
                "or SKIP_ANDROID_SDK=1 to continue without device tooling)"
            )
            if strict and not skip:
                errors.append(msg)
            elif not skip:
                warnings.append(msg)
            else:
                warnings.append("SKIP_ANDROID_SDK=1 — Android SDK preflight bypassed")
    for name in OPTIONAL_TOOLS:
        if not tool_present(name):
            warnings.append(f"{name} not found (optional)")
    return errors, warnings


def apply_license(root: Path, license_id: str) -> Path | None:
    if license_id == "MIT":
        return root / "LICENSE" if (root / "LICENSE").is_file() else None
    src = root / "templates" / "licenses" / f"{license_id}.txt"
    if not src.is_file():
        raise FileNotFoundError(f"license template missing: {src}")
    dest = root / "LICENSE"
    dest.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    return dest
