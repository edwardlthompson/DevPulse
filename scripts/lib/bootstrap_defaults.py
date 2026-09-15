"""Default bootstrap.config.json shape (keeps bootstrap_engine under line budget)."""
from __future__ import annotations

from typing import Any

SCHEMA_VERSION = 1


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
        "crash_proxy": {"enabled": False},
        "crash_inbox": {"enabled": False, "provider": "none"},
    }
