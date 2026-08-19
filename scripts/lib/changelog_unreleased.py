"""CHANGELOG [Unreleased] order and emptiness checks."""
from __future__ import annotations

import re
import sys
from pathlib import Path

UNRELEASED = re.compile(r"^## \[Unreleased\]\s*$", re.M)
VERSION = re.compile(r"^## \[", re.M)
SECTION = re.compile(r"^## \[Unreleased\][ \t]*\n(.*?)(?=^## |\Z)", re.S | re.M)


def section_body(text: str) -> str | None:
    match = SECTION.search(text)
    if not match:
        return None
    return match.group(1)


def unreleased_count(text: str) -> int:
    return len(UNRELEASED.findall(text))


def unreleased_is_first(text: str) -> bool:
    headings = VERSION.findall(text)
    return bool(headings) and text[VERSION.search(text).start() :].startswith(
        "## [Unreleased]"
    )


def unreleased_is_empty(text: str) -> bool:
    body = section_body(text)
    if body is None:
        return False
    return not re.search(r"^### |^\* ", body, re.M)


def extract_notes(text: str) -> str:
    body = section_body(text)
    if body is None:
        return ""
    return body.strip()


def emptied(text: str) -> str:
    match = SECTION.search(text)
    if not match:
        return text
    prefix = text[: match.start()]
    rest = text[match.end() :]
    return f"{prefix}## [Unreleased]\n\n{rest}"


def fold(path: Path) -> str:
    text = path.read_text(encoding="utf-8")
    if section_body(text) is None or unreleased_is_empty(text):
        return ""
    notes = extract_notes(text)
    path.write_text(emptied(text), encoding="utf-8", newline="\n")
    return notes


def check(path: Path, *, require_empty: bool = False) -> list[str]:
    text = path.read_text(encoding="utf-8")
    errors: list[str] = []
    count = unreleased_count(text)
    if count != 1:
        errors.append(f"exactly one ## [Unreleased] required (found {count})")
        return errors
    if not unreleased_is_first(text):
        errors.append("## [Unreleased] must be the first version heading")
    if require_empty and not unreleased_is_empty(text):
        errors.append("## [Unreleased] must be empty before Release Please merge")
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    require_empty = "--require-empty" in args
    do_fold = "--fold" in args
    path = Path("CHANGELOG.md")
    for item in args:
        if item not in {"--require-empty", "--fold"}:
            path = Path(item)
            break
    if do_fold:
        notes = fold(path)
        if notes:
            print(notes)
        errors = check(path, require_empty=require_empty)
        if errors:
            for err in errors:
                print(f"FAIL: {err}", file=sys.stderr)
            return 1
        return 0
    errors = check(path, require_empty=require_empty)
    if errors:
        for err in errors:
            print(f"FAIL: {err}")
        return 1
    print("CHANGELOG Unreleased check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
