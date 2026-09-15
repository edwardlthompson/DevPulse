"""Sanitize CI branch names and release tags (OSPS-BR-01.01 / 01.02)."""
from __future__ import annotations

import os
import re
import sys

BRANCH_RE = re.compile(r"^[A-Za-z0-9._/-]+$")
TAG_RE = re.compile(r"^v?[0-9]+\.[0-9]+\.[0-9]+(?:[.-][A-Za-z0-9.-]+)?$")


def is_safe_ref(name: str) -> bool:
    text = (name or "").strip()
    if not text or ".." in text or text.startswith("-") or text.endswith("/"):
        return False
    return bool(BRANCH_RE.fullmatch(text) or TAG_RE.fullmatch(text))


def is_safe_tag(name: str) -> bool:
    text = (name or "").strip()
    return bool(text) and ".." not in text and bool(TAG_RE.fullmatch(text))


def check_env(env: dict[str, str] | None = None, *, require_tag: bool = False) -> list[str]:
    data = env if env is not None else os.environ
    errors: list[str] = []
    branch = (data.get("GITHUB_REF_NAME") or "").strip()
    if branch and not is_safe_ref(branch):
        errors.append(f"unsafe GITHUB_REF_NAME: {branch!r}")
    tag = (data.get("INPUT_TAG") or "").strip()
    if tag and not is_safe_tag(tag):
        errors.append(f"unsafe INPUT_TAG: {tag!r}")
    elif require_tag and tag and not is_safe_tag(tag):
        errors.append(f"unsafe INPUT_TAG: {tag!r}")
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    require_tag = "--release" in args
    errors = check_env(require_tag=require_tag)
    if errors:
        print("CI ref check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("CI refs OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
