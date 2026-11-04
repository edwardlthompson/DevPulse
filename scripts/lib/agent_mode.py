"""Repo-wide Agent Mode (speed / balanced / lean) — ADR-0010."""
from __future__ import annotations

import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

MODES = frozenset({"speed", "balanced", "lean"})
DEFAULT_MODE = "balanced"
CONFIG_REL = Path(".cursor") / "agent-mode.json"
OPEN_CLOUD = re.compile(r"(🔲\s+\[AGENT\])\[CLOUD\]")
POLICY = {
    "speed": "Cloud allowed; prefer fast parallel when scoped.",
    "balanced": "LOCAL preferred; Cloud allowed when venue says so.",
    "lean": "Cloud & Grok Bots blocked; prefer LOCAL sequential; brief replies.",
}
INJECTION = {
    "speed": "AGENT_MODE=speed · Cloud allowed · prefer fast scoped parallel · brief replies",
    "balanced": "AGENT_MODE=balanced · LOCAL preferred · Cloud allowed when tagged · brief replies",
    "lean": "AGENT_MODE=lean · Cloud & Grok Bots blocked · prefer LOCAL sequential · light gates · brief replies",
}


def config_path(root: Path) -> Path:
    return root / CONFIG_REL


def load_mode(root: Path) -> dict:
    path = config_path(root)
    fallback = {
        "mode": DEFAULT_MODE,
        "updated": "",
        "updated_by": "default",
        "effective": True,
    }
    if not path.is_file():
        return fallback
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError):
        fallback["updated_by"] = "invalid-json"
        return fallback
    mode = str(data.get("mode") or DEFAULT_MODE).strip().lower()
    if mode not in MODES:
        mode = DEFAULT_MODE
    return {
        "mode": mode,
        "updated": str(data.get("updated") or ""),
        "updated_by": str(data.get("updated_by") or "unknown"),
        "effective": False,
    }


def write_mode(root: Path, mode: str, *, updated_by: str = "human") -> dict:
    mode = mode.strip().lower()
    if mode not in MODES:
        raise ValueError(f"invalid mode {mode!r}; expected speed|balanced|lean")
    now = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    payload = {"mode": mode, "updated": now, "updated_by": updated_by}
    path = config_path(root)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    return payload


def convert_cloud_to_local(text: str) -> tuple[str, int]:
    count = 0

    def _sub(match: re.Match[str]) -> str:
        nonlocal count
        count += 1
        return f"{match.group(1)}[LOCAL]"

    return OPEN_CLOUD.sub(_sub, text), count


def convert_build_plans(root: Path) -> int:
    total = 0
    for rel in ("BUILD_PLAN.md", "BUILD_PLAN_TEMPLATE.md"):
        path = root / rel
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        new, n = convert_cloud_to_local(text)
        if n:
            path.write_text(new, encoding="utf-8", newline="\n")
            total += n
    return total


def refuse_cloud_message() -> str:
    return (
        "REFUSED: Agent mode is lean — Cloud Agents and Grok Bots are blocked. "
        "Switch with /speed or /balanced."
    )


def set_agent_mode(root: Path, mode: str, *, updated_by: str = "set-agent-mode") -> int:
    payload = write_mode(root, mode, updated_by=updated_by)
    print(f"Agent mode: {payload['mode']}")
    print(f"Policy: {POLICY[payload['mode']]}")
    print(f"Updated: {payload['updated']} by {payload['updated_by']}")
    if payload["mode"] == "lean":
        print(f"Converted open CLOUD→LOCAL rows: {convert_build_plans(root)}")
        print("Blocked while lean: Cloud Agents, Grok Bots")
    return 0


def show_mode(root: Path) -> int:
    info = load_mode(root)
    print(f"mode={info['mode']}")
    print(POLICY[info["mode"]])
    return 0


def main(argv: list[str] | None = None) -> int:
    args = list(argv if argv is not None else sys.argv[1:])
    root = Path.cwd()
    if args and args[0] in ("-C", "--root") and len(args) >= 2:
        root, args = Path(args[1]).resolve(), args[2:]
    if not args or args[0] in ("-h", "--help"):
        print("Usage: set-agent-mode --mode speed|balanced|lean [--by NAME] | --show")
        return 0 if args and args[0] in ("-h", "--help") else 1
    if args[0] == "--show":
        return show_mode(root)
    mode, by, i = "", "set-agent-mode", 0
    while i < len(args):
        if args[i] == "--mode" and i + 1 < len(args):
            mode, i = args[i + 1], i + 2
        elif args[i] == "--by" and i + 1 < len(args):
            by, i = args[i + 1], i + 2
        elif args[i] in MODES and not mode:
            mode, i = args[i], i + 1
        else:
            print(f"Unknown arg: {args[i]}", file=sys.stderr)
            return 1
    if not mode:
        print("ERROR: --mode speed|balanced|lean required", file=sys.stderr)
        return 1
    try:
        return set_agent_mode(root, mode, updated_by=by)
    except ValueError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
