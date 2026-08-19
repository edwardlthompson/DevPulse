#!/usr/bin/env python3
"""Normalize markdown whitespace: strip trailing space, max one blank line between blocks."""
from __future__ import annotations

import sys
from pathlib import Path


def is_table_row(line: str) -> bool:
    return line.strip().startswith("|")


def normalize_markdown(text: str) -> str:
    lines = [line.rstrip() for line in text.splitlines()]
    out: list[str] = []
    in_fence = False
    blank_run = 0
    prev_was_table_row = False

    for line in lines:
        if line.startswith("```"):
            in_fence = not in_fence
            if out and out[-1] == "" and blank_run:
                pass
            else:
                if out and out[-1] != "":
                    out.append("")
            out.append(line)
            blank_run = 0
            prev_was_table_row = False
            continue

        if in_fence:
            out.append(line)
            continue

        current_is_table_row = is_table_row(line)

        if line == "":
            if prev_was_table_row:
                blank_run += 1
                continue
            blank_run += 1
            if blank_run == 1:
                out.append("")
            continue

        blank_run = 0
        out.append(line)
        prev_was_table_row = current_is_table_row

    result = "\n".join(out)
    if result and not result.endswith("\n"):
        result += "\n"
    return result


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    targets = sys.argv[1:] or ["README.md", "BUILD_PLAN.md"]
    for rel in targets:
        path = root / rel
        if not path.is_file():
            print(f"SKIP missing: {rel}")
            continue
        original = path.read_text(encoding="utf-8")
        normalized = normalize_markdown(original)
        path.write_text(normalized, encoding="utf-8", newline="\n")
        print(f"normalized: {rel}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
