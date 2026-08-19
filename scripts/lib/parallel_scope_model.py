"""Types and overlap helpers for Parallel BUILD_PLAN scopes."""
from __future__ import annotations

import re
from dataclasses import dataclass

MAX_AGENTS = 8

FORBIDDEN_PATHS = [
    "BUILD_PLAN.md",
    "COMPLETED_TASKS.md",
    "examples/web/src/appBootstrap.ts",
    "examples/web/src/main.ts",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/GoldenPathApp.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/MainActivity.kt",
]

SPRINT_HEADER = re.compile(r"^#{2,3}\s+Sprint\s+", re.I)
PARALLEL_HEADER = re.compile(r"^#{3,4}\s+.*Parallel", re.I | re.MULTILINE)
SEQUENTIAL_HEADER = re.compile(r"^#{3,4}\s+Sequential", re.I | re.MULTILINE)
TABLE_ROW = re.compile(r"^\|([^|]+)\|([^|]+)\|([^|]+)\|")
OPEN_AGENT_SEQ = re.compile(
    r"^(?:\d+[a-z]?)\.\s+(?:🔲|⬜|\[ \])\s+\[AGENT\]\s+",
)
AGENT_COUNT_TARGET = re.compile(r"<!--\s*agent_count_target:\s*(\d+)", re.I)
PARALLEL_EXCEPTION = re.compile(r"<!--\s*parallel_exception:\s*(.+?)\s*-->", re.I)


@dataclass
class ParallelRow:
    task: str
    owner: str
    scope: str
    sprint_title: str = ""


@dataclass
class SprintBlock:
    title: str
    lines: list[str]
    start: int
    agent_count_target: int | None
    parallel_exception: str | None


def normalize_scope(scope: str) -> str:
    return scope.strip().rstrip("/")


def scopes_overlap(a: str, b: str) -> bool:
    pa, pb = normalize_scope(a), normalize_scope(b)
    if not pa or not pb:
        return False
    return pa == pb or pa.startswith(pb + "/") or pb.startswith(pa + "/")


def find_overlaps(scopes: list[str]) -> list[str]:
    errors: list[str] = []
    for i, a in enumerate(scopes):
        for j, b in enumerate(scopes):
            if j <= i:
                continue
            if scopes_overlap(a, b):
                errors.append(f"overlap: {a!r} vs {b!r}")
    return errors


def slugify(text: str) -> str:
    text = text.lower().strip()
    out: list[str] = []
    for ch in text.replace("/", "-").replace("_", "-"):
        if ch.isalnum():
            out.append(ch.lower())
        elif ch in " -":
            if out and out[-1] != "-":
                out.append("-")
    return "".join(out).strip("-")[:48] or "task"
