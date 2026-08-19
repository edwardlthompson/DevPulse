#!/usr/bin/env python3
"""Cross-platform UTF-8 / UTF-16 BOM check for tracked text files."""
from __future__ import annotations

import os
import sys

EXTS = {
    ".md", ".json", ".yml", ".yaml", ".sh", ".ps1", ".mdc", ".toml",
    ".ts", ".tsx", ".html", ".css", ".properties", ".kts", ".kt", ".xml",
    ".webmanifest",
}
ROOT_TEXT_FILES = {"LICENSE", ".template-version", "CODEOWNERS"}
SKIP_DIRS = {".git", "node_modules", ".venv", "dist", "coverage", "target"}


def is_bad_encoding(path: str) -> bool:
    with open(path, "rb") as f:
        b = f.read(4)
    if b.startswith(b"\xff\xfe") or b.startswith(b"\xfe\xff"):
        return True
    if len(b) >= 2 and b[1] == 0 and b[0] < 128:
        return True
    return False


def iter_files(root: str, targets: list[str] | None) -> list[str]:
    if targets:
        return [t for t in targets if os.path.isfile(t)]
    found: list[str] = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for fn in filenames:
            if fn in ROOT_TEXT_FILES or os.path.splitext(fn)[1].lower() in EXTS:
                found.append(os.path.join(dirpath, fn))
    return found


def main() -> int:
    root = sys.argv[1] if len(sys.argv) > 1 else os.getcwd()
    targets = sys.argv[2:] if len(sys.argv) > 2 else None
    errors: list[str] = []

    gitignore = os.path.join(root, ".gitignore")
    if os.path.isfile(gitignore) and is_bad_encoding(gitignore):
        errors.append(".gitignore")

    for name in ROOT_TEXT_FILES:
        fp = os.path.join(root, name)
        if os.path.isfile(fp) and is_bad_encoding(fp):
            errors.append(name)

    for fp in iter_files(root, targets):
        if is_bad_encoding(fp):
            errors.append(os.path.relpath(fp, root))

    if errors:
        print("Invalid encoding (require UTF-8 without BOM):")
        for e in sorted(errors):
            print(f"  {e}")
        return 1

    print("File encoding check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
