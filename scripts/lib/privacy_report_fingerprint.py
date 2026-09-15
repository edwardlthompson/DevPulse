"""Stable crash fingerprint from a sanitized stack."""
from __future__ import annotations

import hashlib
import re

from privacy_report_sanitize import sanitize_report_text


def fingerprint_crash(stack: str | None, exception_type: str | None = None) -> str:
    cleaned = sanitize_report_text(stack, stack=True)
    frames = [ln.strip() for ln in cleaned.splitlines() if ln.strip()][:12]
    kind = (exception_type or _guess_type(cleaned) or "Error").strip()
    payload = kind + "\n" + "\n".join(frames)
    digest = hashlib.sha256(payload.encode("utf-8")).hexdigest()
    return digest[:12]


def _guess_type(stack: str) -> str:
    first = stack.splitlines()[0].strip() if stack.strip() else ""
    match = re.match(r"^([A-Za-z][A-Za-z0-9_.$]+)", first)
    return match.group(1) if match else "Error"
