"""Pure-read /status report — ADR-0010 companion."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from agent_mode import DEFAULT_MODE, INJECTION, POLICY, config_path, load_mode
from agent_venue import parse_open_agent_rows


def _json_file(path: Path) -> dict | None:
    if not path.is_file():
        return None
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError):
        return None
    return data if isinstance(data, dict) else None


def _field(root: Path, key: str) -> str:
    for rel in (".cursor/stack-selection.json", "bootstrap.config.json"):
        data = _json_file(root / rel)
        if data and data.get(key):
            return str(data[key])
    return "unknown"


def _board_summary(root: Path) -> tuple[int, int, str]:
    path = root / "BUILD_PLAN.md"
    if not path.is_file():
        return 0, 0, "board empty"
    text = path.read_text(encoding="utf-8")
    local = cloud = 0
    next_row = "board empty"
    for venue, _body, line in parse_open_agent_rows(text):
        if venue == "LOCAL":
            local += 1
        elif venue == "CLOUD":
            cloud += 1
        if next_row == "board empty" and venue in ("LOCAL", "CLOUD"):
            next_row = line[:100] + ("…" if len(line) > 100 else "")
    return local, cloud, next_row


def _venue_policy(mode: str) -> str:
    return {
        "lean": "LOCAL only (Cloud blocked)",
        "speed": "Cloud allowed",
    }.get(mode, "LOCAL preferred")


def _cfg_line(path: Path, summary: str) -> str:
    rel = path.as_posix() if path.is_absolute() else str(path)
    try:
        rel = path.resolve().relative_to(Path.cwd().resolve()).as_posix()
    except ValueError:
        rel = path.as_posix()
    return f"  • {rel} → {summary if path.is_file() else 'missing'}"


def render_status(root: Path) -> str:
    info = load_mode(root)
    mode = info["mode"]
    local_n, cloud_n, next_row = _board_summary(root)
    mode_path, stack_path = config_path(root), root / ".cursor" / "stack-selection.json"
    boot_path = root / "bootstrap.config.json"
    sel, boot = _json_file(stack_path), _json_file(boot_path)
    mode_sum = f"mode={mode}" + (" (effective default)" if info.get("effective") else "")
    stack_sum = (
        f"stack={sel.get('stack')} tier={sel.get('distribution_tier')}" if sel else "missing"
    )
    boot_sum = (
        f"project={boot.get('project_name')} stack={boot.get('stack')}" if boot else "missing"
    )
    return "\n".join(
        [
            "Repo Status Report",
            "──────────────────",
            f"Agent mode:          {mode}",
            f"  Policy:            {POLICY.get(mode, 'unknown')}",
            f"Distribution tier:   {_field(root, 'distribution_tier')}",
            f"Golden Path / stacks: {_field(root, 'stack')}",
            f"Venue policy:        {_venue_policy(mode)}",
            "Local compute:       not probed",
            "Key config files:",
            _cfg_line(mode_path, mode_sum),
            _cfg_line(stack_path, stack_sum),
            _cfg_line(boot_path, boot_sum),
            "Cost-diet / rules:   thin always-on (ADR-0009) + agent-mode injection",
            f"Last mode change:    {info.get('updated') or 'unknown'} + {info.get('updated_by') or 'unknown'}",
            "Open BUILD_PLAN summary:",
            f"  LOCAL: {local_n} open",
            f"  CLOUD: {cloud_n} open",
            f"  Next row: {next_row}",
            "Relevant ADRs:       0008 (venue) · 0009 (cost diet) · 0010 (agent mode)",
            f"Injection:           {INJECTION.get(mode, INJECTION[DEFAULT_MODE])}",
        ]
    ) + "\n"


def main(argv: list[str] | None = None) -> int:
    args = list(argv if argv is not None else sys.argv[1:])
    root = Path.cwd()
    if args and args[0] in ("-C", "--root") and len(args) >= 2:
        root = Path(args[1]).resolve()
    print(render_status(root), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
