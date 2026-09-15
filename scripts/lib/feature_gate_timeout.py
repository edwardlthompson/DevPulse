"""Per-stack feature-gate command timeouts (seconds)."""
from __future__ import annotations

import os
import sys

DEFAULTS = {
    "web": 300,
    "python": 240,
    "android": 600,
    "node": 240,
    "rust": 300,
    "go": 180,
    "lightroom": 120,
    "blender": 240,
    "docs": 180,
    "multi": 900,
}

STAGE_PREFIXES = (
    "web",
    "python",
    "android",
    "node",
    "rust",
    "go",
    "lightroom",
    "blender",
)


def stack_for_stage(stage: str) -> str:
    for prefix in STAGE_PREFIXES:
        if stage == prefix or stage.startswith(f"{prefix}-"):
            return prefix
    return "docs"


def timeout_seconds(stack: str, env: dict[str, str] | None = None) -> int:
    env = os.environ if env is None else env
    global_raw = env.get("FEATURE_GATE_TIMEOUT", "").strip()
    if global_raw.isdigit() and int(global_raw) > 0:
        return int(global_raw)
    key = f"FEATURE_GATE_TIMEOUT_{stack.upper()}"
    specific = env.get(key, "").strip()
    if specific.isdigit() and int(specific) > 0:
        return int(specific)
    return DEFAULTS.get(stack, DEFAULTS["docs"])


def main(argv: list[str] | None = None) -> int:
    args = list(sys.argv[1:] if argv is None else argv)
    stack = "docs"
    if "--stack" in args:
        stack = args[args.index("--stack") + 1]
    elif "--stage" in args:
        stack = stack_for_stage(args[args.index("--stage") + 1])
    print(timeout_seconds(stack))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
