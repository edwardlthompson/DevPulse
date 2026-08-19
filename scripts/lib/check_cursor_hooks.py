"""Validate .cursor/hooks.json and optional smoke tests."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from check_cursor_hooks_smoke import smoke

FORBIDDEN_EVENTS = frozenset(
    {
        "beforeSubmitPrompt",
        "afterAgentThought",
        "preCompact",
    }
)

REQUIRED_HOOKS = (
    "sessionStart",
    "beforeShellExecution",
    "afterFileEdit",
    "subagentStart",
    "beforeMCPExecution",
)


def hooks_disabled(root: Path) -> bool:
    bp = root / "BUILD_PLAN.md"
    if not bp.is_file():
        return False
    return "<!-- cursor-hooks: off -->" in bp.read_text(encoding="utf-8")


def load_hooks(root: Path) -> dict:
    path = root / ".cursor/hooks.json"
    if not path.is_file():
        raise FileNotFoundError("missing .cursor/hooks.json")
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("version") != 1:
        raise ValueError("hooks.json version must be 1")
    return data


def resolve_hook_script(root: Path, command: str) -> Path | None:
    cmd = command.strip()
    for prefix in ("python3 ", "python "):
        if cmd.startswith(prefix):
            cmd = cmd[len(prefix) :].strip()
            break
    script = root / cmd
    if script.is_file():
        return script
    return None


def validate(root: Path) -> list[str]:
    errors: list[str] = []
    if hooks_disabled(root):
        return errors
    try:
        data = load_hooks(root)
    except (FileNotFoundError, json.JSONDecodeError, ValueError) as exc:
        return [str(exc)]

    hooks = data.get("hooks") or {}
    for event in hooks:
        if event in FORBIDDEN_EVENTS:
            errors.append(f"forbidden hook event: {event}")

    for event in REQUIRED_HOOKS:
        entries = hooks.get(event) or []
        if not entries:
            errors.append(f"missing hook event: {event}")
            continue
        for entry in entries:
            cmd = entry.get("command", "")
            script = resolve_hook_script(root, cmd)
            if script is None:
                errors.append(f"hook script missing: {cmd}")
                continue
            if script.suffix not in (".py", ".sh"):
                errors.append(f"hook script must be .py or .sh: {cmd}")
                continue
            first_line = script.read_text(encoding="utf-8").splitlines()[0:1]
            if not first_line or not first_line[0].startswith("#!"):
                errors.append(f"hook script shebang must be line 1: {cmd}")

    return errors


def main() -> int:
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--smoke", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()

    errors = validate(root)
    if args.smoke:
        errors.extend(smoke(root))

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        return 1
    print("Cursor hooks check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
