#!/usr/bin/env python3
"""Enforce AGENTS.md file line limits on tracked files only."""
from __future__ import annotations

import fnmatch
import subprocess
import sys
from pathlib import Path

STATIC_LIMIT = 300
LOGIC_LIMIT = 150
STATIC_GLOBS = (
    "*.tsx",
    "*.jsx",
    "*.vue",
    "*_view.*",
    "examples/web/src/components/*.ts",
    "examples/android/app/src/main/java/*/ui/*/*.kt",
    "examples/android/app/src/main/java/*/ui/GoldenPath*.kt",
    "examples/*/locales/*.json",
    "examples/*/src/locales/*.json",
    "examples/*/res/values/strings.xml",
    "examples/*/res/values-*/strings.xml",
)
STATIC_SKIP = {
    "package.json",
    "package-lock.json",
    "tsconfig.json",
    ".lighthouserc.json",
}
LOGIC_SKIP_PREFIX = (
    "examples/web/src/components/",
    "examples/web/src/main.ts",
    "examples/web/src/appBootstrap.ts",
)
LIB_ALLOWLIST: set[str] = set()


def tracked_files(root: Path) -> list[str]:
    out = subprocess.check_output(["git", "ls-files"], cwd=root, text=True)
    return [line.strip() for line in out.splitlines() if line.strip()]


def line_count(path: Path) -> int:
    return len(path.read_text(encoding="utf-8", errors="replace").splitlines())


def is_static(rel: str) -> bool:
    name = Path(rel).name
    if name in STATIC_SKIP:
        return False
    return any(fnmatch.fnmatch(rel, pat) for pat in STATIC_GLOBS)


def is_example_logic(rel: str) -> bool:
    if not rel.startswith("examples/"):
        return False
    if not rel.endswith((".ts", ".py", ".kt")):
        return False
    if ".test." in rel or ".spec." in rel:
        return False
    if rel == "examples/web/src/main.ts" or any(rel.startswith(p) for p in LOGIC_SKIP_PREFIX):
        return False
    if fnmatch.fnmatch(rel, "examples/android/app/src/main/java/*/ui/*"):
        return False
    return True


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    errors = 0
    files = tracked_files(root)
    print(f"Checking static data file limits (max {STATIC_LIMIT} lines)...")
    for rel in files:
        path = root / rel
        if not path.is_file() or not is_static(rel):
            continue
        n = line_count(path)
        if n > STATIC_LIMIT:
            print(f"FAIL [static-data] {rel}: {n} lines (max {STATIC_LIMIT})")
            errors += 1
    print(f"Checking scripts/lib logic file limits (max {LOGIC_LIMIT} lines)...")
    for rel in files:
        if not rel.startswith("scripts/lib/") or not rel.endswith(".py"):
            continue
        if rel in LIB_ALLOWLIST or Path(rel).name in LIB_ALLOWLIST:
            continue
        path = root / rel
        if not path.is_file():
            continue
        n = line_count(path)
        if n > LOGIC_LIMIT:
            print(f"FAIL [logic] {rel}: {n} lines (max {LOGIC_LIMIT})")
            errors += 1
    print(f"Checking pure logic file limits (max {LOGIC_LIMIT} lines)...")
    for rel in files:
        if not is_example_logic(rel):
            continue
        path = root / rel
        if not path.is_file():
            continue
        n = line_count(path)
        if n > LOGIC_LIMIT:
            print(f"FAIL [logic] {rel}: {n} lines (max {LOGIC_LIMIT})")
            errors += 1
    if errors:
        print(f"{errors} file(s) exceed line limits")
        return 1
    print("All file line limits OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
