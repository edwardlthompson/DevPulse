"""Build sanitized markdown for a GitHub crash/bug/feature report."""
from __future__ import annotations

from privacy_report_sanitize import sanitize_report_text

KINDS = frozenset({"crash", "bug", "feature"})


def build_report_markdown(
    kind: str,
    description: str | None,
    *,
    stack: str | None = None,
    exception_type: str | None = None,
    fingerprint: str | None = None,
    app_version: str | None = None,
    os_family: str | None = None,
) -> str:
    report_kind = kind if kind in KINDS else "bug"
    desc = sanitize_report_text(description)
    stack_s = sanitize_report_text(stack, stack=True)
    parts = [
        f"## What happened",
        desc or "(no description)",
        "",
        f"## Kind",
        report_kind,
    ]
    if fingerprint:
        parts.extend(["", "## Fingerprint", f"`{sanitize_report_text(fingerprint)}`"])
    if exception_type:
        parts.extend(["", "## Exception", sanitize_report_text(exception_type)])
    if app_version:
        parts.extend(["", "## App version", sanitize_report_text(app_version)])
    if os_family:
        parts.extend(["", "## OS family", sanitize_report_text(os_family)])
    if stack_s:
        parts.extend(["", "## Stack", "```", stack_s, "```"])
    return "\n".join(parts).strip() + "\n"
