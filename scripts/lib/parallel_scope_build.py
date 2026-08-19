"""Build parallel dispatch manifests from BUILD_PLAN.md."""
from __future__ import annotations

from pathlib import Path

from parallel_scope_ctx import resolve_context, substitute_placeholders, suggest_rows
from parallel_scope_model import FORBIDDEN_PATHS, MAX_AGENTS, find_overlaps, slugify
from parallel_scope_parse import (
    agent_rows,
    parse_parallel_rows,
    sequential_agent_open,
)


def build_manifest(
    root: Path,
    build_plan_path: Path,
    *,
    stack: str | None = None,
    feature: str | None = None,
    require_sequential_clear: bool = False,
    suggest: bool = False,
) -> dict:
    text = build_plan_path.read_text(encoding="utf-8")
    ctx = resolve_context(root, stack=stack, feature=feature)
    all_rows = parse_parallel_rows(text)
    agents_raw = agent_rows(all_rows)
    if feature or ctx.get("feature"):
        agents_raw = [
            r
            for r in agents_raw
            if "{feature}" in r.scope
            or "2+" in r.sprint_title
            or "Incremental" in r.sprint_title
            or "Per-feature" in r.sprint_title
        ]

    blockers: list[str] = []
    if require_sequential_clear:
        open_seq = sequential_agent_open(text)
        if open_seq:
            blockers.append(
                f"{len(open_seq)} open [AGENT] Sequential item(s) before Parallel lane"
            )

    agents: list[dict] = []
    labels = "ABCDEFGH"
    unresolved = False
    for idx, row in enumerate(agents_raw):
        if idx >= MAX_AGENTS:
            break
        try:
            scope = substitute_placeholders(row.scope, ctx)
        except ValueError as exc:
            blockers.append(str(exc))
            unresolved = True
            scope = row.scope
        agent_id = labels[idx] if idx < len(labels) else str(idx + 1)
        branch = f"feature/agent-{slugify(row.task)}"
        agents.append(
            {
                "id": agent_id,
                "task": row.task,
                "owner": row.owner,
                "scope": scope,
                "branch": branch,
                "sprint": row.sprint_title,
                "forbidden_paths": FORBIDDEN_PATHS,
            }
        )

    suggestions: list[dict] = []
    if suggest or len(agents) < 2:
        for row in suggest_rows(ctx):
            try:
                scope = substitute_placeholders(row.scope, ctx)
            except ValueError:
                scope = row.scope.replace("{feature}", "*")
            suggestions.append(
                {"task": row.task, "owner": row.owner, "scope": scope}
            )

    scopes = [a["scope"] for a in agents]
    overlaps = find_overlaps(scopes)
    if overlaps:
        blockers.extend(overlaps)

    agent_count = len(agents)
    split_hint: str | None = None
    if agent_count > MAX_AGENTS:
        split_hint = (
            f"Split sprint into sub-sprints; table implies {agent_count} agents "
            f"(max {MAX_AGENTS})"
        )
        blockers.append(split_hint)
        agents = agents[:MAX_AGENTS]
        agent_count = len(agents)

    ready = not blockers and agent_count > 0
    return {
        "agent_count": agent_count,
        "ready": ready,
        "blockers": blockers,
        "agents": agents,
        "suggestions": suggestions,
        "split_hint": split_hint,
        "context": {"stack": ctx["stack"], "feature": ctx.get("feature", "")},
        "unresolved_placeholders": unresolved,
    }
