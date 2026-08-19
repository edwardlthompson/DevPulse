"""Bootstrap manifest, preflight checks, and license application."""
from __future__ import annotations

import json
import shutil
from pathlib import Path
from typing import Any

SCHEMA_VERSION = 1
LICENSES = ("MIT", "Apache-2.0")
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


def default_config(
    *,
    project_name: str = "",
    purpose: str = "",
    stack: str = "none",
    license_id: str = "MIT",
    distribution_tier: str = "foss",
) -> dict[str, Any]:
    return {
        "schema_version": SCHEMA_VERSION,
        "project_name": project_name,
        "purpose": purpose,
        "stack": stack,
        "license": license_id,
        "distribution_tier": distribution_tier,
        "agent_adapters": {
            "cursor_rules": True,
            "claude": True,
            "copilot": True,
            "gemini": True,
            "windsurf": True,
            "cline": True,
            "aider": True,
            "continue": True,
        },
        "security": {
            "dependabot": True,
            "code_scanning": True,
            "secret_detection": True,
        },
        "hooks": {
            "preflight": True,
            "post_sync_adapters": True,
            "post_checklist": True,
            "post_git_init": False,
            "post_git_commit": False,
            "post_install_deps": False,
            "post_run_tests": False,
            "post_welcome_issue": False,
        },
    }


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
    extras = STACK_TOOLS.get(stack, ())
    for name in extras:
        if not tool_present(name):
            msg = f"{name} not found (recommended for stack {stack})"
            if strict:
                errors.append(msg)
            else:
                warnings.append(msg)
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
