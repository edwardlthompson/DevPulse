"""Redact PII and secrets from crash/bug report text."""
from __future__ import annotations

import re

MAX_BODY_BYTES = 8192
MAX_STACK_LINES = 200

_PEM = re.compile(
    r"-----BEGIN [A-Z ]*PRIVATE KEY-----.*?-----END [A-Z ]*PRIVATE KEY-----",
    re.S,
)
_GITHUB = re.compile(r"\b(?:ghp|gho|github_pat)_[A-Za-z0-9_]+")
_BEARER = re.compile(r"Bearer\s+\S+", re.I)
_JWT = re.compile(r"\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+")
_AWS = re.compile(r"\bAKIA[0-9A-Z]{16}\b")
_API = re.compile(r"(?i)(?:api[_-]?key|token)\s*[:=]\s*\S+")
_EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
_WIN_HOME = re.compile(r"(?i)C:\\Users\\[^\\]+\\")
_UNIX_HOME = re.compile(r"/(?:home|Users)/[^/\s]+/")
_UNC = re.compile(r"\\\\[^\\\s]+\\[^\\\s]+\\")
_IPV4 = re.compile(r"\b(?:\d{1,3}\.){3}\d{1,3}\b")
_IPV6 = re.compile(r"\b(?:[0-9a-f]{1,4}:){2,7}[0-9a-f]{1,4}\b", re.I)
_URL_Q = re.compile(r"([?&])(token|key|code|access_token)=[^&\s]+", re.I)


def sanitize_report_text(text: str | None, *, stack: bool = False) -> str:
    if text is None:
        return ""
    out = str(text)
    out = _PEM.sub("<redacted-secret>", out)
    out = _GITHUB.sub("<redacted-secret>", out)
    out = _BEARER.sub("<redacted-secret>", out)
    out = _JWT.sub("<redacted-secret>", out)
    out = _AWS.sub("<redacted-secret>", out)
    out = _API.sub("<redacted-secret>", out)
    out = _EMAIL.sub("<redacted-email>", out)
    out = _WIN_HOME.sub("<redacted-home>", out)
    out = _UNIX_HOME.sub("<redacted-home>/", out)
    out = _UNC.sub("<redacted-unc>", out)
    out = _IPV4.sub("<redacted-ip>", out)
    out = _IPV6.sub("<redacted-ip>", out)
    out = _URL_Q.sub(r"\1\2=<redacted-secret>", out)
    if stack:
        out = "\n".join(out.splitlines()[:MAX_STACK_LINES])
    return _cap_whole_lines(out)


def _cap_whole_lines(text: str) -> str:
    encoded = text.encode("utf-8")
    if len(encoded) <= MAX_BODY_BYTES:
        return text
    kept: list[str] = []
    size = 0
    for line in text.splitlines():
        add = len(line.encode("utf-8")) + 1
        if size + add > MAX_BODY_BYTES:
            break
        kept.append(line)
        size += add
    return "\n".join(kept)
