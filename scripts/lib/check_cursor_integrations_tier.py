"""Tier-specific Cursor integration checks."""
from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path

COMMERCIAL_LIVE = (
    ".cursor/BUGBOT.md",
    ".cursor/environment.json",
    ".cursor/approval-policies",
)


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def validate_tier(root: Path, tier: str) -> list[str]:
    errors: list[str] = []
    warnings: list[str] = []

    if tier == "foss":
        for rel in COMMERCIAL_LIVE:
            if (root / rel).exists():
                errors.append(f"foss tier: commercial live file present: {rel}")
        mcp = root / ".cursor/mcp.json"
        if mcp.is_file():
            tracked = subprocess.run(
                ["git", "ls-files", "--error-unmatch", ".cursor/mcp.json"],
                cwd=root,
                capture_output=True,
                text=True,
                check=False,
            )
            if tracked.returncode == 0:
                errors.append(
                    "foss tier: .cursor/mcp.json is tracked — keep live MCP config gitignored"
                )

    if tier == "commercial":
        for rel in (".cursor/BUGBOT.md", ".cursor/environment.json"):
            if not (root / rel).is_file():
                warnings.append(
                    f"commercial tier: {rel} not activated (copy from *.commercial.example)"
                )

    sel = root / ".cursor/stack-selection.json"
    manifest = root / ".cursor/cursor-features.json"
    if sel.is_file() and manifest.is_file():
        try:
            sel_tier = json.loads(read_text(sel)).get("distribution_tier", "foss")
            man_tier = json.loads(read_text(manifest)).get("distribution_tier", "foss")
            if sel_tier != man_tier:
                errors.append("cursor-features.json tier mismatch with stack-selection.json")
            if sel_tier != tier:
                errors.append(f"stack-selection tier {sel_tier} != requested --tier {tier}")
        except json.JSONDecodeError:
            errors.append("invalid stack-selection or cursor-features JSON")

    for warn in warnings:
        print(f"WARN: {warn}", file=sys.stderr)
    return errors
