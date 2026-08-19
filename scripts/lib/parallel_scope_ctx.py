"""Context and suggestion helpers for parallel dispatch."""
from __future__ import annotations

import json
from pathlib import Path

from parallel_scope_model import MAX_AGENTS, ParallelRow


def load_json(path: Path) -> dict | None:
    if not path.exists():
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return None


def resolve_context(
    root: Path,
    *,
    stack: str | None = None,
    feature: str | None = None,
) -> dict[str, str | list[str]]:
    ctx: dict[str, str | list[str]] = {
        "stack": stack or "web",
        "feature": feature or "",
        "active_modules": [],
    }
    progress = load_json(root / ".cursor/agent-progress.json")
    if progress and not feature and progress.get("current_feature"):
        ctx["feature"] = str(progress["current_feature"])
    selection = load_json(root / ".cursor/stack-selection.json")
    if selection:
        if not stack and selection.get("stack"):
            ctx["stack"] = str(selection["stack"])
        modules = selection.get("active_modules")
        if isinstance(modules, list):
            ctx["active_modules"] = [str(m) for m in modules]
    if not ctx["active_modules"]:
        ctx["active_modules"] = [str(ctx["stack"])]
    return ctx


def substitute_placeholders(scope: str, ctx: dict[str, str | list[str]]) -> str:
    result = scope.replace("{stack}", str(ctx.get("stack", "web")))
    feature = str(ctx.get("feature", ""))
    if "{feature}" in result and not feature:
        raise ValueError(
            f"Unresolved {{feature}} in scope {scope!r}. "
            "Set current_feature via agent-progress.sh or pass --feature."
        )
    return result.replace("{feature}", feature)


def suggest_rows(ctx: dict[str, str | list[str]]) -> list[ParallelRow]:
    stack = str(ctx.get("stack", "web"))
    feature = str(ctx.get("feature", ""))
    modules = ctx.get("active_modules") or [stack]
    if len(modules) > 1:
        return [
            ParallelRow(task=f"{mod} stack slice", owner="AGENT", scope=f"examples/{mod}/**")
            for mod in modules
        ][:MAX_AGENTS]
    if feature:
        return [
            ParallelRow(task="Logic + unit tests", owner="AGENT", scope=f"examples/{stack}/src/{feature}/"),
            ParallelRow(task="View + i18n", owner="AGENT", scope=f"examples/{stack}/src/components/"),
        ]
    return [
        ParallelRow(task="App code", owner="AGENT", scope=f"examples/{stack}/**"),
        ParallelRow(task="Docs + module guides", owner="AGENT", scope="docs/**"),
    ]
