"""Onboarding docs: bold glossary jargon must link to GLOSSARY.md."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ONBOARDING = (
    Path("docs/START_HERE.md"),
    Path("docs/help/TOUR.md"),
    Path(".cursor/commands/tour.md"),
)
REQUIRED = ("Sacred", "Canon", "AGENT", "HUMAN", "ADB", "AUTO")
ROW_RE = re.compile(r"^\|\s*\*\*(.+?)\*\*\s*\|", re.M)
BOLD_RE = re.compile(r"\*\*(.+?)\*\*")
LINK_RE = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")


def glossary_terms(text: str) -> set[str]:
    terms: set[str] = set()
    for match in ROW_RE.finditer(text):
        raw = match.group(1).strip()
        parts = re.split(r"\s+vs\s+|\s+/\s+", raw)
        for part in parts:
            cleaned = re.sub(r"\s*\([^)]*\)\s*$", "", part).strip().strip("`")
            if cleaned:
                terms.add(cleaned)
    return terms


def _covered_spans(markdown: str) -> list[tuple[int, int]]:
    spans: list[tuple[int, int]] = []
    for match in LINK_RE.finditer(markdown):
        if "glossary.md" in match.group(2).lower():
            spans.append((match.start(), match.end()))
    return spans


def _norm_bold(inner: str) -> str:
    return inner.strip().strip("`").rstrip(":").strip()


def unlinked_jargon(markdown: str, terms: set[str]) -> list[str]:
    covered = _covered_spans(markdown)
    errors: list[str] = []
    for match in BOLD_RE.finditer(markdown):
        inner = match.group(1).strip()
        if inner.startswith("["):
            continue
        key = _norm_bold(inner)
        if key not in terms:
            continue
        if any(start <= match.start() and match.end() <= end for start, end in covered):
            continue
        errors.append(key)
    return errors


def missing_required(markdown: str, required: tuple[str, ...] = REQUIRED) -> list[str]:
    missing: list[str] = []
    for term in required:
        pattern = re.compile(
            r"\[\*\*" + re.escape(term) + r"\*\*\]\([^)]*GLOSSARY\.md[^)]*\)",
            re.I,
        )
        if not pattern.search(markdown):
            missing.append(term)
    return missing


def check_files(root: Path) -> list[str]:
    glossary = root / "docs" / "help" / "GLOSSARY.md"
    if not glossary.is_file():
        return ["MISSING: docs/help/GLOSSARY.md"]
    terms = glossary_terms(glossary.read_text(encoding="utf-8"))
    errors: list[str] = []
    for rel in ONBOARDING:
        path = root / rel
        if not path.is_file():
            errors.append(f"MISSING: {rel.as_posix()}")
            continue
        text = path.read_text(encoding="utf-8")
        for term in unlinked_jargon(text, terms):
            errors.append(f"{rel.as_posix()}: unlinked **{term}**")
        for term in missing_required(text):
            errors.append(f"{rel.as_posix()}: missing linked **{term}**")
    return errors


def main() -> int:
    root = Path.cwd()
    errors = check_files(root)
    if errors:
        print("Glossary link check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Glossary link check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
