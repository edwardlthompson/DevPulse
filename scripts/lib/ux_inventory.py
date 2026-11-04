"""Parse and validate BUILD_PLAN UX inventory blocks."""
from __future__ import annotations

import re
from pathlib import Path

BEGIN = "<!-- ux-inventory:begin -->"
END = "<!-- ux-inventory:end -->"
HEADING = re.compile(r"^### (UX-\d{3}) — (.+)$", re.M)
STATUS = re.compile(r"^\s*-?\s*\*\*Status:\*\*\s*(\S+)", re.M | re.I)
FORBIDDEN = ("backlog", "deferred", "later", "phase-2", "optional")
ALLOWED = frozenset({"planned", "in_progress", "done"})
FIELDS = (
    "Status",
    "Source",
    "Severity",
    "Effort",
    "Impact",
    "Where",
    "What's wrong",
    "Why",
    "Fix",
    "Pattern",
    "Success",
)
EMPTY = "_No UX inventory items._"


def block(text: str) -> str | None:
    start = text.find(BEGIN)
    end = text.find(END)
    if start < 0 or end < 0 or end <= start:
        return None
    return text[start + len(BEGIN) : end]


def item_ids(body: str) -> list[str]:
    return [m.group(1) for m in HEADING.finditer(body)]


def next_id(body: str) -> str:
    nums = [int(i.split("-")[1]) for i in item_ids(body)]
    return f"UX-{max(nums, default=0) + 1:03d}"


def check_text(text: str, *, label: str) -> list[str]:
    errors: list[str] = []
    if BEGIN not in text or END not in text:
        errors.append(f"{label}: missing ux-inventory markers")
        return errors
    body = block(text) or ""
    stripped = body.strip()
    if not HEADING.search(body):
        if stripped not in ("", EMPTY):
            errors.append(f"{label}: empty inventory must be {EMPTY}")
        return errors
    seen: set[str] = set()
    chunks = re.split(r"(?=^### UX-\d{3} — )", body, flags=re.M)
    for chunk in chunks:
        match = HEADING.search(chunk)
        if not match:
            continue
        uid = match.group(1)
        if uid in seen:
            errors.append(f"{label}: duplicate {uid}")
        seen.add(uid)
        st = STATUS.search(chunk)
        status = (st.group(1) if st else "").strip().lower().strip("`")
        if status in FORBIDDEN or status not in ALLOWED:
            errors.append(f"{label}: {uid} forbidden or missing Status ({status or 'empty'})")
        for field in FIELDS:
            if f"**{field}:**" not in chunk:
                errors.append(f"{label}: {uid} missing **{field}:**")
    return errors


def check_paths(paths: list[Path]) -> list[str]:
    errors: list[str] = []
    for path in paths:
        if not path.is_file():
            errors.append(f"MISSING: {path}")
            continue
        errors.extend(check_text(path.read_text(encoding="utf-8"), label=path.name))
    return errors


def main() -> int:
    root = Path.cwd()
    paths = [root / "BUILD_PLAN.md", root / "BUILD_PLAN_TEMPLATE.md"]
    errors = check_paths([p for p in paths if p.is_file()])
    if errors:
        print("\n".join(errors))
        return 1
    print("UX inventory check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
