"""Fail when docs/** and root *.md relative links do not resolve."""
from __future__ import annotations

import re
import sys
from pathlib import Path

SKIP_PREFIXES = ("http://", "https://", "mailto:", "#")
LINK_RE = re.compile(r"\[[^\]]*\]\(([^)]+)\)")


# Historical dumps; first-run and support files stay in the gate.
_SKIP_ROOT = frozenset({"COMPLETED_TASKS.md", "CHANGELOG.md"})


def iter_docs(root: Path) -> list[Path]:
    files: list[Path] = []
    docs = root / "docs"
    if docs.is_dir():
        files.extend(p for p in docs.rglob("*.md") if p.is_file())
    files.extend(
        p
        for p in root.glob("*.md")
        if p.is_file() and p.name not in _SKIP_ROOT
    )
    return sorted(set(files))


def _href_path(raw: str) -> str:
    target = raw.strip()
    if not target:
        return ""
    if target.startswith(SKIP_PREFIXES):
        return ""
    if " " in target:
        target = target.split()[0].strip("\"'")
    return target.split("#", 1)[0].split("?", 1)[0]


def _is_pruned_stack_target(root: Path, dest: Path) -> bool:
    """True when dest is under modules/<stack> or examples/<stack> that was removed."""
    try:
        rel = dest.relative_to(root.resolve())
    except ValueError:
        return False
    parts = rel.parts
    if len(parts) < 2 or parts[0] not in ("modules", "examples"):
        return False
    return not (root / parts[0] / parts[1]).exists()


def check_file(path: Path, root: Path | None = None) -> list[str]:
    errors: list[str] = []
    base = root.resolve() if root is not None else None
    text = path.read_text(encoding="utf-8")
    for match in LINK_RE.finditer(text):
        rel = _href_path(match.group(1))
        if not rel:
            continue
        dest = (path.parent / rel).resolve()
        if dest.exists():
            continue
        if base is not None and _is_pruned_stack_target(base, dest):
            continue
        shown = path.as_posix()
        errors.append(f"{shown}: broken link {match.group(1).strip()}")
    return errors


def collect_errors(root: Path) -> list[str]:
    errors: list[str] = []
    for path in iter_docs(root):
        errors.extend(check_file(path, root))
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    root = Path(args[0] if args else ".").resolve()
    files = iter_docs(root)
    errors = collect_errors(root)
    if errors:
        for item in errors[:40]:
            print(f"FAIL: {item}")
        extra = len(errors) - 40
        if extra > 0:
            print(f"FAIL: {extra} more broken docs link(s)")
        return 1
    print(f"OK   markdown relative links ({len(files)} files)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
