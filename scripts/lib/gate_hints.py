"""Plain-English hints for feature-gate and verify.sh failures."""
from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Any

_DATA_PATH = Path(__file__).with_name("gate_hints.json")
_FALLBACK = {
    "means": "A quality gate failed in the active feature scope.",
    "run": "bash scripts/feature-autofix.sh (or fix the errors shown above)",
    "why": "Local gates are the same checks CI will run on your pull request.",
    "suggested": ["run scripts/feature-autofix.sh", "fix errors in active feature scope"],
}


def _load() -> dict[str, Any]:
    if not _DATA_PATH.is_file():
        return {"default": _FALLBACK, "stages": {}}
    data = json.loads(_DATA_PATH.read_text(encoding="utf-8"))
    return data if isinstance(data, dict) else {"default": _FALLBACK, "stages": {}}


def hint_for(stage: str) -> dict[str, Any]:
    data = _load()
    raw = (data.get("stages") or {}).get(stage) or data.get("default") or _FALLBACK
    suggested = raw.get("suggested") or _FALLBACK["suggested"]
    return {
        "means": str(raw.get("means") or _FALLBACK["means"]),
        "run": str(raw.get("run") or _FALLBACK["run"]),
        "why": str(raw.get("why") or _FALLBACK["why"]),
        "suggested": [str(item) for item in suggested],
    }


def format_human(stage: str, log_tail: str = "") -> str:
    hint = hint_for(stage or "unknown")
    lines = [
        f"What failed: {stage or 'unknown'}",
        f"What that means: {hint['means']}",
        f"What to run: {hint['run']}",
        f"Why: {hint['why']}",
    ]
    tail = (log_tail or "").strip()
    if tail:
        lines.append(f"Log: {tail[:400]}")
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    as_json = False
    if args and args[0] == "--json":
        as_json = True
        args = args[1:]
    stage = args[0] if args else "unknown"
    log_tail = args[1] if len(args) > 1 else ""
    if as_json:
        payload = hint_for(stage)
        payload["human_hint"] = format_human(stage, log_tail)
        print(json.dumps(payload, indent=2))
    else:
        print(format_human(stage, log_tail))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
