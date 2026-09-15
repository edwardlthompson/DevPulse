"""Interpret the first failure from verify.sh / tour-verify output."""
from __future__ import annotations

import argparse
from pathlib import Path


def extract_hint_block(log: str) -> str | None:
    marker = "What failed:"
    idx = log.find(marker)
    if idx < 0:
        return None
    lines: list[str] = []
    for line in log[idx:].splitlines():
        stripped = line.strip()
        if lines and not stripped:
            break
        if stripped:
            lines.append(stripped)
        if len(lines) >= 6:
            break
    return "\n".join(lines) if lines else None


def extract_first_failure(log: str) -> str:
    hint = extract_hint_block(log)
    if hint:
        return hint
    for line in log.splitlines():
        stripped = line.strip()
        if stripped.startswith(("FAIL:", "MISSING:", "EMPTY:", "ERROR:")):
            return stripped
    for line in log.splitlines():
        if "failed" in line.lower() and "Verification harness passed" not in line:
            return line.strip()
    return "unknown (see full verify log)"


def format_report(exit_code: int, log: str) -> str:
    if exit_code == 0:
        return (
            "Tour verify: passed.\n"
            "Next: /coach (or docs/help/COACH.md) for the next BUILD_PLAN row."
        )
    first = extract_first_failure(log)
    return (
        "Tour verify: first failure\n"
        f"{first}\n"
        "Next: /fix for mechanical lint/tests; /debug if strikes >= 3."
    )


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Print the first verify.sh failure.")
    parser.add_argument("--exit", dest="exit_code", type=int, default=0)
    parser.add_argument("--log", default="", help="Path to captured verify output")
    args = parser.parse_args(argv)
    text = ""
    if args.log:
        path = Path(args.log)
        if path.is_file():
            text = path.read_text(encoding="utf-8", errors="replace")
    print(format_report(args.exit_code, text))
    return 0 if args.exit_code == 0 else 1


if __name__ == "__main__":
    import sys

    raise SystemExit(main(sys.argv[1:]))
