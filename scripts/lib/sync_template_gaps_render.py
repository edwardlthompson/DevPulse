"""Render managed BUILD_PLAN template-gaps-sync block from gap report()."""
from __future__ import annotations

from typing import Any

BEGIN = "<!-- template-gaps-sync:begin -->"
END = "<!-- template-gaps-sync:end -->"
EMPTY_NOTE = (
    "_No template gaps; .template-version matches upstream "
    "(or template maintainer N/A)._"
)
FILE_CAP = 40


def extract_inner(text: str) -> str | None:
    begin = text.find(BEGIN)
    end = text.find(END)
    if begin < 0 or end < 0 or end < begin:
        return None
    return text[begin + len(BEGIN) : end].strip("\n")


def replace_inner(text: str, inner: str) -> str:
    begin = text.find(BEGIN)
    end = text.find(END)
    if begin < 0 or end < 0 or end < begin:
        raise ValueError(f"missing {BEGIN} / {END} markers")
    start = begin + len(BEGIN)
    return text[:start] + "\n" + inner + "\n" + text[end:]


def _file_rows(files: list[dict[str, Any]], *, cap: int) -> tuple[list[str], int]:
    rows: list[str] = []
    with_path = [f for f in files if str(f.get("path") or "").strip()]
    for item in with_path:
        if len(rows) >= cap:
            break
        path = str(item.get("path") or "").strip()
        policy = str(item.get("policy") or "mixed").lower()
        if policy == "canon":
            rows.append(f"- 🔲 [AGENT] Canon: {path}")
        elif policy == "sacred":
            rows.append(f"- 🔲 [HUMAN] Sacred: {path} (never blind-overwrite)")
        else:
            rows.append(f"- 🔲 [AGENT] Mixed: {path}")
    return rows, max(0, len(with_path) - len(rows))


def _feature_rows(features: list[dict[str, Any]]) -> list[str]:
    rows: list[str] = []
    for feat in features:
        fid = str(feat.get("id") or "").strip() or "unknown"
        title = str(feat.get("title") or "").strip() or fid
        spec = str(feat.get("spec") or "").strip()
        label = f"{fid} — {title}"
        if spec:
            rows.append(f"- 🔲 [AGENT] Feature gap: [{label}]({spec})")
        else:
            rows.append(f"- 🔲 [AGENT] Feature gap: {label}")
    return rows


def render_inner(report: dict[str, Any], *, template_repo: bool = False) -> str:
    if template_repo:
        return EMPTY_NOTE
    skip = [str(s) for s in (report.get("skip") or []) if str(s).strip()]
    if skip:
        return f"_Template gap sync skipped: {'; '.join(skip)}_"
    warning = str(report.get("warning") or "").strip()
    current = str(report.get("current") or "").strip()
    latest = str(report.get("latest") or "").strip()
    upstream = str(report.get("upstream") or "").strip()
    files = [f for f in (report.get("files") or []) if isinstance(f, dict)]
    features = [f for f in (report.get("features") or []) if isinstance(f, dict)]
    file_rows, omitted = _file_rows(files, cap=FILE_CAP)
    feat_rows = _feature_rows(features)

    if not current or not latest:
        return f"_Template gap sync skipped: {warning or 'missing version'}_"
    if current == latest and not file_rows and not feat_rows:
        return EMPTY_NOTE

    head = f"Parent `{current}` → `{latest}`"
    if upstream:
        head += f" ({upstream})"
    parts: list[str] = [head]
    if file_rows:
        parts.append("")
        parts.append("#### Canon / Mixed / Sacred")
        parts.extend(file_rows)
        if omitted:
            parts.append(f"_…and {omitted} more — run `check-template-gaps`._")
    if feat_rows:
        parts.append("")
        parts.append("#### Golden Path features")
        parts.extend(feat_rows)
    if warning:
        parts.append("")
        parts.append(f"_Note: {warning}_")
    # Behind with no file/feature rows: version banner only (no fabricated 🔲).
    return "\n".join(parts)
