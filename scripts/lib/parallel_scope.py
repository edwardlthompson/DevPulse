"""Parse BUILD_PLAN Parallel tables, detect scope overlaps, build dispatch manifests."""
from __future__ import annotations

from pathlib import Path

from parallel_scope_build import build_manifest
from parallel_scope_ctx import resolve_context, substitute_placeholders, suggest_rows
from parallel_scope_model import (
    FORBIDDEN_PATHS,
    MAX_AGENTS,
    PARALLEL_HEADER,
    ParallelRow,
    SprintBlock,
    find_overlaps,
    normalize_scope,
    scopes_overlap,
    slugify,
)
from parallel_scope_parse import (
    agent_rows,
    parse_parallel_rows,
    parse_sprint_blocks,
    sequential_agent_open,
)

__all__ = [
    "FORBIDDEN_PATHS",
    "MAX_AGENTS",
    "ParallelRow",
    "SprintBlock",
    "agent_rows",
    "build_manifest",
    "check_build_plan_parallel",
    "find_overlaps",
    "normalize_scope",
    "parse_parallel_rows",
    "parse_sprint_blocks",
    "resolve_context",
    "scopes_overlap",
    "sequential_agent_open",
    "slugify",
    "substitute_placeholders",
    "suggest_rows",
]


def check_build_plan_parallel(
    build_plan_path: Path,
    root: Path | None = None,
    *,
    min_agents: int = 2,
) -> tuple[bool, list[str]]:
    text = build_plan_path.read_text(encoding="utf-8")
    repo_root = root or build_plan_path.parent
    ctx = resolve_context(repo_root)
    errors: list[str] = []
    blocks = parse_sprint_blocks(text)
    if not blocks:
        return True, []

    for block in blocks:
        body = "\n".join(block.lines)
        if not PARALLEL_HEADER.search(body):
            if block.parallel_exception:
                continue
            errors.append(f"{block.title}: missing ### Parallel table")
            continue
        sprint_rows = parse_parallel_rows(body)
        agent_list = agent_rows(sprint_rows)
        scopes = []
        for row in agent_list:
            try:
                scopes.append(substitute_placeholders(row.scope, ctx))
            except ValueError:
                scopes.append(row.scope)
        errors.extend(f"{block.title}: {e}" for e in find_overlaps(scopes))
        exc = block.parallel_exception
        if exc and exc.lower() not in ("none", ""):
            continue
        if len(agent_list) < min_agents:
            errors.append(
                f"{block.title}: {len(agent_list)} AGENT Parallel row(s) "
                f"(need >= {min_agents} or <!-- parallel_exception: reason -->)"
            )
        if block.agent_count_target and block.agent_count_target > MAX_AGENTS:
            errors.append(
                f"{block.title}: agent_count_target {block.agent_count_target} exceeds max {MAX_AGENTS}"
            )
    return len(errors) == 0, errors
