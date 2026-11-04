"""Parse and validate BUILD_PLAN LOCAL/CLOUD agent venues."""
from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from parallel_scope_model import find_overlaps, normalize_scope

LOCAL_BEGIN, LOCAL_END = "<!-- local-agent-lane:begin -->", "<!-- local-agent-lane:end -->"
CLOUD_BEGIN, CLOUD_END = "<!-- cloud-agent-lane:begin -->", "<!-- cloud-agent-lane:end -->"
EMPTY_LOCAL, EMPTY_CLOUD = "_No local agent items._", "_No cloud agent items._"
VENUES = frozenset({"LOCAL", "CLOUD"})
SKIP = object()
OPEN_AGENT = re.compile(
    r"^(?:(?:\d+[a-z]?)\.|-)\s+(?P<status>🔲|❌)\s+"
    r"\[AGENT\](?:\[(?P<venue>LOCAL|CLOUD)\])?\s+(?P<body>.+)$"
)
SCOPE_TRAILER = re.compile(r"(?:—|--|-)\s*scope:\s*(?P<scope>\S.+?)\s*$", re.I)
PARALLEL_OWNER = re.compile(r"^\|([^|]+)\|([^|]+)\|([^|]+)\|")
PARALLEL_HEADER = re.compile(r"^#{3,4}\s+.*Parallel", re.I)

@dataclass(frozen=True)
class VenueRow:
    venue: str
    scope: str
    task: str
    line: str

def _lane_body(text: str, begin: str, end: str) -> str | None:
    start, stop = text.find(begin), text.find(end)
    if start < 0 or stop < 0 or stop <= start:
        return None
    return text[start + len(begin) : stop]

def _owner_venue(owner: str) -> str | None | object:
    u = owner.upper().replace(" ", "")
    if not u.startswith("AGENT"):
        return SKIP
    if "[LOCAL]" in u or u.endswith("LOCAL"):
        return "LOCAL"
    if "[CLOUD]" in u or u.endswith("CLOUD"):
        return "CLOUD"
    return None if u == "AGENT" else SKIP

def parse_open_agent_rows(text: str) -> list[tuple[str | None, str, str]]:
    rows: list[tuple[str | None, str, str]] = []
    for line in text.splitlines():
        m = OPEN_AGENT.match(line.strip())
        if m:
            rows.append((m.group("venue"), m.group("body").strip(), line.strip()))
    return rows

def parse_parallel_agent_rows(text: str) -> list[tuple[str | None, str, str]]:
    rows: list[tuple[str | None, str, str]] = []
    in_table = False
    for line in text.splitlines():
        if PARALLEL_HEADER.match(line):
            in_table = True
            continue
        if in_table and line.startswith("#"):
            in_table = False
        if not in_table:
            continue
        m = PARALLEL_OWNER.match(line)
        if not m:
            continue
        task, owner, scope = (c.strip() for c in m.groups())
        if task.lower() in ("task", "---") or not owner:
            continue
        venue = _owner_venue(owner)
        if venue is SKIP:
            continue
        rows.append((venue if isinstance(venue, str) else None, f"{task} — scope: {scope}", line.strip()))
    return rows

def extract_scope(body: str) -> str | None:
    m = SCOPE_TRAILER.search(body)
    return normalize_scope(m.group("scope")) if m else None

def venue_rows(text: str) -> list[VenueRow]:
    out: list[VenueRow] = []
    for venue, body, line in parse_open_agent_rows(text) + parse_parallel_agent_rows(text):
        scope = extract_scope(body) if venue in VENUES else None
        if venue in VENUES and scope:
            out.append(VenueRow(venue=venue, scope=scope, task=body, line=line))
    return out

def check_text(text: str, *, label: str) -> list[str]:
    errors: list[str] = []
    for begin, end, empty, name in (
        (LOCAL_BEGIN, LOCAL_END, EMPTY_LOCAL, "local-agent-lane"),
        (CLOUD_BEGIN, CLOUD_END, EMPTY_CLOUD, "cloud-agent-lane"),
    ):
        if begin not in text or end not in text:
            errors.append(f"{label}: missing {name} markers")
            continue
        body = (_lane_body(text, begin, end) or "").strip()
        if body != empty and "🔲" not in body and "❌" not in body and "✅" not in body:
            errors.append(f"{label}: {name} must be {empty} or venue rows")
    for venue, body, line in parse_open_agent_rows(text) + parse_parallel_agent_rows(text):
        if venue is None:
            errors.append(f"{label}: open AGENT missing [LOCAL]/[CLOUD]: {line[:100]}")
        elif extract_scope(body) is None:
            errors.append(f"{label}: open AGENT[{venue}] missing — scope: {line[:100]}")
    locals_ = [r.scope for r in venue_rows(text) if r.venue == "LOCAL"]
    clouds = [r.scope for r in venue_rows(text) if r.venue == "CLOUD"]
    for local in locals_:
        for cloud in clouds:
            if find_overlaps([local, cloud]):
                errors.append(f"{label}: LOCAL/CLOUD scope overlap {local!r} vs {cloud!r}")
    return errors

def path_under_scope(path: str, scope: str) -> bool:
    p, s = normalize_scope(path.replace("\\", "/")), normalize_scope(scope)
    return bool(p and s) and (s in (".", "*") or p == s or p.startswith(s + "/"))

def local_scopes_hit_by_files(text: str, files: list[str]) -> list[str]:
    return [
        r.scope
        for r in venue_rows(text)
        if r.venue == "LOCAL" and any(path_under_scope(f, r.scope) for f in files)
    ]

def check_paths(paths: list[Path]) -> list[str]:
    errors: list[str] = []
    for path in paths:
        errors.extend(check_text(path.read_text(encoding="utf-8"), label=path.name))
    return errors

def main() -> int:
    root = Path.cwd()
    paths = [p for p in (root / "BUILD_PLAN.md", root / "BUILD_PLAN_TEMPLATE.md") if p.is_file()]
    errors = check_paths(paths)
    if errors:
        print("\n".join(errors))
        return 1
    print("Agent venue check passed")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
