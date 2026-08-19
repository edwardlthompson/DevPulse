"""Split BUILD_PLAN text into sprint and maintenance row groups."""
from __future__ import annotations

from build_sprint_model import (
    HUMAN_GROUP_HEADER,
    PARALLEL_HEADER,
    ROW_BULLET,
    SEQUENTIAL_HEADER,
    SPRINT_HEADER,
    PlanRow,
)


def split_sprint_phases(
    lines: list[str],
) -> tuple[list[str], list[str], list[str], list[str]]:
    pre: list[str] = []
    parallel: list[str] = []
    post: list[str] = []
    human: list[str] = []
    phase = "pre"
    for line in lines:
        if line.startswith("### Sprint") or line.startswith("<!--"):
            continue
        if PARALLEL_HEADER.match(line):
            phase = "parallel"
            parallel.append(line)
            continue
        if HUMAN_GROUP_HEADER.match(line):
            phase = "human"
            human.append(line)
            continue
        if phase == "parallel" and SEQUENTIAL_HEADER.match(line):
            phase = "post"
        if phase == "pre":
            pre.append(line)
        elif phase == "parallel":
            parallel.append(line)
        elif phase == "post":
            post.append(line)
        else:
            human.append(line)
    return pre, parallel, post, human


def parse_sprint_blocks(text: str) -> list[tuple[str, list[str]]]:
    blocks: list[tuple[str, list[str]]] = []
    in_child = False
    i = 0
    lines = text.splitlines()
    while i < len(lines):
        line = lines[i]
        if line.strip().startswith("## Child Repo Playbook"):
            in_child = True
            i += 1
            continue
        if not in_child:
            i += 1
            continue
        if line.startswith("## Ongoing Maintenance"):
            break
        if SPRINT_HEADER.match(line):
            title = line.strip().lstrip("#").strip()
            block_lines: list[str] = [line]
            i += 1
            while i < len(lines) and not (
                SPRINT_HEADER.match(lines[i])
                or (lines[i].startswith("## ") and not lines[i].startswith("### "))
            ):
                block_lines.append(lines[i])
                i += 1
            blocks.append((title, block_lines))
            continue
        i += 1
    return blocks


def parse_maintenance_rows(text: str) -> tuple[list[PlanRow], list[PlanRow]]:
    auto_rows: list[PlanRow] = []
    human_rows: list[PlanRow] = []
    in_maint = False
    in_human = False
    for line in text.splitlines():
        if line.startswith("## Ongoing Maintenance"):
            in_maint = True
            continue
        if in_maint and line.startswith("## ") and not line.startswith("### "):
            break
        if not in_maint:
            continue
        if HUMAN_GROUP_HEADER.match(line):
            in_human = True
            continue
        match = ROW_BULLET.match(line)
        if not match:
            continue
        row = PlanRow(
            owner=match.group("owner"),
            task=match.group("task").strip(),
            sprint="Ongoing Maintenance",
            phase="human_group" if in_human else "maintenance",
        )
        if in_human:
            human_rows.append(row)
        else:
            auto_rows.append(row)
    return auto_rows, human_rows
