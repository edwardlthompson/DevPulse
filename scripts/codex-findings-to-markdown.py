#!/usr/bin/env python3
"""Convert Codex findings JSON to CODE_REVIEW.md (UTF-8)."""
from __future__ import annotations

import argparse
import json
import sys
from datetime import date
from pathlib import Path
from typing import Any


SEVERITIES = ("Critical", "High", "Medium", "Low", "Deferred")
SOURCES = ("codex-ci", "codex-local", "audit")


def validate(data: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    if not isinstance(data, dict):
        return ["root must be an object"]
    source = data.get("source")
    if source not in SOURCES:
        errors.append(f"source must be one of {SOURCES}")
    summary = data.get("summary")
    if not isinstance(summary, str) or not summary.strip():
        errors.append("summary must be a non-empty string")
    findings = data.get("findings")
    if not isinstance(findings, list):
        errors.append("findings must be an array")
        return errors
    for i, item in enumerate(findings):
        if not isinstance(item, dict):
            errors.append(f"findings[{i}] must be an object")
            continue
        for key in ("id", "severity", "area", "finding", "recommendation"):
            if key not in item or not str(item.get(key, "")).strip():
                errors.append(f"findings[{i}].{key} required")
        sev = item.get("severity")
        if sev is not None and sev not in SEVERITIES:
            errors.append(f"findings[{i}].severity invalid: {sev}")
        fid = item.get("id")
        if isinstance(fid, str) and not (
            fid.startswith("F-") and len(fid) == 5 and fid[2:].isdigit()
        ):
            errors.append(f"findings[{i}].id must match F-NNN: {fid}")
    return errors


def to_markdown(data: dict[str, Any], *, source: str | None, head_sha: str | None) -> str:
    src = source or data.get("source") or "codex-local"
    summary = str(data.get("summary", "")).strip() or "No summary provided."
    sha = head_sha if head_sha is not None else data.get("head_sha")
    findings = data.get("findings") or []
    today = date.today().isoformat()

    lines = [
        f"# Code Review — {today}",
        "",
        f"**Source:** `{src}`",
    ]
    if sha:
        lines.append(f"**head_sha:** `{sha}`")
    lines += [
        "",
        "## Summary",
        "",
        summary,
        "",
        "## Findings",
        "",
        "| ID | Severity | Area | Finding | Recommendation |",
        "|----|----------|------|---------|----------------|",
    ]
    if not findings:
        lines.append("| — | — | — | No findings | — |")
    else:
        for item in findings:
            path = item.get("path")
            line = item.get("line")
            area = str(item.get("area", ""))
            if path:
                loc = f"{path}" + (f":{line}" if line else "")
                area = f"{area} (`{loc}`)" if area else f"`{loc}`"
            lines.append(
                "| {id} | {sev} | {area} | {finding} | {rec} |".format(
                    id=item.get("id", ""),
                    sev=item.get("severity", ""),
                    area=area.replace("|", "\\|"),
                    finding=str(item.get("finding", "")).replace("|", "\\|"),
                    rec=str(item.get("recommendation", "")).replace("|", "\\|"),
                )
            )
    lines += [
        "",
        "**Severity:** Critical · High · Medium · Low · Deferred",
        "",
        "## BUILD_PLAN links",
        "",
        "Map Critical/High finding IDs to 🔲 [AGENT] rows in `BUILD_PLAN.md`, then run `/fix`.",
        "",
        "## Out of scope",
        "",
        "Low/Deferred items may wait unless they block release.",
        "",
    ]
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", "-i", required=True, help="Codex findings JSON path")
    parser.add_argument("--output", "-o", required=True, help="CODE_REVIEW.md path")
    parser.add_argument("--source", choices=SOURCES, default=None)
    parser.add_argument("--head-sha", default=None)
    parser.add_argument("--allow-empty", action="store_true", help="Accept empty findings")
    args = parser.parse_args()

    in_path = Path(args.input)
    if not in_path.is_file():
        print(f"FAIL: input not found: {in_path}", file=sys.stderr)
        return 1

    try:
        raw = json.loads(in_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        print(f"FAIL: invalid JSON: {exc}", file=sys.stderr)
        return 1

    if not isinstance(raw, dict):
        print("FAIL: root must be a JSON object", file=sys.stderr)
        return 1

    if args.source:
        raw["source"] = args.source
    if args.head_sha:
        raw["head_sha"] = args.head_sha

    errors = validate(raw)
    if errors:
        for err in errors:
            print(f"FAIL: {err}", file=sys.stderr)
        return 1

    if not raw.get("findings") and not args.allow_empty:
        # Empty findings is a valid soft pass; still write the markdown.
        pass

    md = to_markdown(raw, source=args.source, head_sha=args.head_sha)
    out = Path(args.output)
    out.write_text(md, encoding="utf-8", newline="\n")
    print(f"Wrote {out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
