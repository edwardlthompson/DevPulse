"""Apply child Linguist attribution block onto .gitattributes during init."""
from __future__ import annotations

from pathlib import Path

MARKER = "# --- linguist ---"
SCRIPTS_LINE = "scripts/** linguist-vendored"
PATTERNS = (
    "scripts/** linguist-vendored",
    "modules/** linguist-documentation",
    "docs/** linguist-documentation",
    "examples/*/build/** linguist-vendored",
    ".github/** linguist-vendored",
)


def child_template(root: Path) -> Path:
    return root / "templates" / "linguist" / "child.gitattributes"


def render_block(stack: str, template_text: str) -> str:
    """Return the linguist block body; comment scripts/** for python stacks."""
    lines: list[str] = []
    for raw in template_text.splitlines():
        line = raw.rstrip("\n")
        stripped = line.strip()
        if stack == "python" and stripped == SCRIPTS_LINE:
            lines.append(
                f"# {stripped}  # python stack: keep product scripts in language stats"
            )
            continue
        lines.append(line)
    text = "\n".join(lines)
    if not text.endswith("\n"):
        text += "\n"
    return text


def apply_linguist_attrs(
    root: Path,
    *,
    stack: str = "none",
    dry_run: bool = False,
    skip: bool = False,
) -> str:
    """Copy or append child linguist rules onto root/.gitattributes.

    Returns a short human status line (also printed by callers for dry-run).
    """
    if skip:
        return "skip linguist (.gitattributes): --no-linguist"

    src = child_template(root)
    if not src.is_file():
        return f"skip linguist: missing {src.as_posix()}"

    block = render_block(stack, src.read_text(encoding="utf-8"))
    dest = root / ".gitattributes"
    if dest.is_file():
        existing = dest.read_text(encoding="utf-8")
        if MARKER in existing and all(p in existing for p in PATTERNS):
            msg = f"linguist: keep existing {dest.as_posix()} (marker present)"
            return msg
        # If patterns already present without marker, still treat as done.
        if all(
            p in existing or f"# {p}" in existing
            for p in PATTERNS
        ):
            return f"linguist: keep existing {dest.as_posix()} (patterns present)"
        addition = f"\n{MARKER}\n{block}"
        if not existing.endswith("\n"):
            addition = "\n" + addition
        msg = f"linguist: append block under {MARKER} → {dest.as_posix()}"
        if dry_run:
            return f"dry-run {msg}"
        dest.write_text(existing.rstrip() + addition, encoding="utf-8")
        return msg

    msg = f"linguist: copy {src.as_posix()} → {dest.as_posix()}"
    if dry_run:
        return f"dry-run {msg}"
    dest.write_text(block, encoding="utf-8")
    return msg
