"""Parse Parallel tables and sprint blocks from BUILD_PLAN.md."""
from __future__ import annotations

import re

from parallel_scope_model import (
    AGENT_COUNT_TARGET,
    OPEN_AGENT_SEQ,
    PARALLEL_EXCEPTION,
    PARALLEL_HEADER,
    SPRINT_HEADER,
    TABLE_ROW,
    ParallelRow,
    SprintBlock,
)

def parse_parallel_rows(text: str) -> list[ParallelRow]:
    rows: list[ParallelRow] = []
    current_sprint = ""
    in_parallel = False
    for line in text.splitlines():
        if SPRINT_HEADER.match(line):
            current_sprint = line.strip().lstrip("#").strip()
            in_parallel = False
            continue
        if PARALLEL_HEADER.match(line):
            in_parallel = True
            continue
        if in_parallel and line.startswith("#"):
            in_parallel = False
            continue
        if not in_parallel:
            continue
        m = TABLE_ROW.match(line)
        if not m or m.group(1).strip().lower() in ("task", "---"):
            continue
        task, owner, scope_cell = m.group(1).strip(), m.group(2).strip(), m.group(3).strip()
        scope_m = re.search(r"`([^`]+)`", scope_cell)
        if not scope_m:
            continue
        rows.append(
            ParallelRow(
                task=task,
                owner=owner.upper(),
                scope=scope_m.group(1).strip(),
                sprint_title=current_sprint,
            )
        )
    return rows


def parse_sprint_blocks(text: str) -> list[SprintBlock]:
    """Return sprint sections from Child Repo Playbook (### Sprint ...) onward."""
    lines = text.splitlines()
    start_idx = 0
    for i, line in enumerate(lines):
        if line.strip().startswith("## Child Repo Playbook"):
            start_idx = i
            break
    blocks: list[SprintBlock] = []
    i = start_idx
    while i < len(lines):
        line = lines[i]
        if SPRINT_HEADER.match(line):
            title = line.strip().lstrip("#").strip()
            block_lines = [line]
            i += 1
            while i < len(lines) and not (
                SPRINT_HEADER.match(lines[i])
                or (
                    lines[i].startswith("## ")
                    and not lines[i].startswith("### ")
                )
            ):
                block_lines.append(lines[i])
                i += 1
            body = "\n".join(block_lines)
            target_m = AGENT_COUNT_TARGET.search(body)
            exc_m = PARALLEL_EXCEPTION.search(body)
            blocks.append(
                SprintBlock(
                    title=title,
                    lines=block_lines,
                    start=i,
                    agent_count_target=int(target_m.group(1)) if target_m else None,
                    parallel_exception=exc_m.group(1).strip() if exc_m else None,
                )
            )
            continue
        i += 1
    return blocks


def agent_rows(rows: list[ParallelRow]) -> list[ParallelRow]:
    return [r for r in rows if r.owner == "AGENT"]


def sequential_agent_open(text: str, before_parallel: bool = True) -> list[str]:
    """Open AGENT sequential items before first Parallel section in child playbook."""
    lines = text.splitlines()
    in_child = False
    seen_parallel = False
    open_items: list[str] = []
    for line in lines:
        if line.strip().startswith("## Child Repo Playbook"):
            in_child = True
            continue
        if not in_child:
            continue
        if line.startswith("## Ongoing Maintenance"):
            break
        if PARALLEL_HEADER.match(line):
            seen_parallel = True
            if before_parallel:
                break
            continue
        if seen_parallel and before_parallel:
            break
        if OPEN_AGENT_SEQ.match(line):
            open_items.append(line.strip())
    return open_items

