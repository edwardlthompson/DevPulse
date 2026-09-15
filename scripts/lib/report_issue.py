"""Compose a sanitized GitHub issue body. Never invent a token."""
from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path

LIB = Path(__file__).resolve().parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from privacy_report_fingerprint import fingerprint_crash  # noqa: E402
from privacy_report_markdown import build_report_markdown  # noqa: E402
from privacy_report_sanitize import sanitize_report_text  # noqa: E402


def os_family() -> str:
    return {"nt": "Windows", "posix": os.name}.get(os.name, os.name)


def compose(kind: str, description: str, stack: str, version: str) -> tuple[str, str]:
    fp = fingerprint_crash(stack, None) if stack else ""
    title = f"[crash] {fp} Error" if kind == "crash" and fp else f"[{kind}]: "
    body = build_report_markdown(
        kind,
        description,
        stack=stack or None,
        fingerprint=fp or None,
        app_version=version or None,
        os_family=os_family(),
    )
    return title, body


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Sanitized GitHub issue composer")
    parser.add_argument("--type", choices=("crash", "bug", "feature"), default="bug")
    parser.add_argument("--description", default="")
    parser.add_argument("--stack", default="")
    parser.add_argument("--version", default="")
    parser.add_argument("--print", action="store_true", dest="print_only")
    args = parser.parse_args(argv)
    title, body = compose(args.type, args.description, args.stack, args.version)
    if args.print_only or not shutil.which("gh"):
        print(title)
        print(body)
        if not args.print_only:
            print("gh not found; copy the markdown or open the issue form URL.")
        return 0
    tmp = Path(".cursor") / "report-issue-body.md"
    tmp.parent.mkdir(parents=True, exist_ok=True)
    tmp.write_text(sanitize_report_text(body), encoding="utf-8")
    proc = subprocess.run(
        ["gh", "issue", "create", "--title", title, "--body-file", str(tmp)],
        check=False,
    )
    return proc.returncode


if __name__ == "__main__":
    raise SystemExit(main())
