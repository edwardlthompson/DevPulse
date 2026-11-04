"""Validate Cursor FOSS integration artifacts and tier compliance."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from check_cursor_integrations_tier import validate_tier
from cursor_rule_audit import audit_rules

SKILLS = (
    "validate-bootstrap",
    "parallel-scope",
    "watch-gates-autofix",
    "check-repo-hygiene",
    "sprint0-signoff",
    "feature-vertical-slice",
    "canvas-bootstrap-status",
    "update-deps",
    "best-of-n",
    "local-models",
    "emulator",
    "adr",
    "ux-review",
    "blender-icons",
)
AGENTS = ("verifier", "gate-fixer", "explorer")
COMMAND_SKILL = {
    "gates.md": ("validate-bootstrap", "check-repo-hygiene", "canvas-bootstrap-status"),
    "scope.md": ("parallel-scope",),
    "fix.md": ("watch-gates-autofix",),
    "audit.md": ("check-repo-hygiene",),
    "feature.md": ("feature-vertical-slice",),
    "update-deps.md": ("update-deps",),
    "best-of-n.md": ("best-of-n",),
    "emulator.md": ("emulator",),
    "adr.md": ("adr",),
    "ux-review.md": ("ux-review",),
    "ux-apply.md": ("ux-review",),
}

FOSS_EXAMPLES = (
    ".cursor/mcp.foss.example",
    ".cursor/hooks.json",
    ".cursor/worktrees.json",
    ".cursor/permissions.json",
    ".cursor/mcp-allowlist.json",
)


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def validate_artifacts(root: Path) -> list[str]:
    errors: list[str] = []
    for rel in FOSS_EXAMPLES:
        if not (root / rel).is_file():
            errors.append(f"missing: {rel}")

    for name in SKILLS:
        skill = root / f".cursor/skills/{name}/SKILL.md"
        if not skill.is_file():
            errors.append(f"missing skill: {name}")
            continue
        body = read_text(skill)
        if "See also:" not in body:
            errors.append(f"skill {name} missing See also: link")

    for name in AGENTS:
        agent = root / f".cursor/agents/{name}.md"
        if not agent.is_file():
            errors.append(f"missing agent: {name}")

    for cmd, skills in COMMAND_SKILL.items():
        path = root / ".cursor/commands" / cmd
        if not path.is_file():
            errors.append(f"missing command: {cmd}")
            continue
        body = read_text(path)
        for skill in skills:
            if skill not in body:
                errors.append(f"{cmd} missing skill pointer for {skill}")

    registry = root / "docs/CURSOR_FEATURE_REGISTRY.json"
    if not registry.is_file():
        errors.append("missing docs/CURSOR_FEATURE_REGISTRY.json")
    else:
        try:
            data = json.loads(read_text(registry))
            entries = data.get("entries") or []
            if not entries:
                errors.append("registry has no entries")
            if str(data.get("updated_at") or "") < "2026-09-10":
                errors.append("registry updated_at is stale")
            ids = {entry.get("id") for entry in entries}
            for entry in entries:
                if "distribution_tier" not in entry:
                    errors.append(f"registry entry missing distribution_tier: {entry.get('id')}")
            skills_dir = root / ".cursor" / "skills"
            if skills_dir.is_dir():
                for path in sorted(skills_dir.iterdir()):
                    if path.is_dir() and (path / "SKILL.md").is_file():
                        key = f"skills.{path.name}"
                        if key not in ids:
                            errors.append(f"registry missing {key}")
        except json.JSONDecodeError as exc:
            errors.append(f"invalid registry JSON: {exc}")

    return errors


def main() -> int:
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--tier", default="foss", choices=("foss", "commercial"))
    args = parser.parse_args()
    root = Path(args.root).resolve()

    errors = validate_artifacts(root)
    errors.extend(validate_tier(root, args.tier))
    errors.extend(audit_rules(root))

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        return 1
    print(f"Cursor integrations check passed (tier={args.tier})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
