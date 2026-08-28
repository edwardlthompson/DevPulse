"""Required GitHub check-name rollup (branch protection contexts)."""
from __future__ import annotations

from typing import Iterable

REQUIRED_CONTEXTS: tuple[str, ...] = (
    "CI",
    "Security Scan",
    "CodeQL",
    "Repo Hygiene",
    "Feature Gate",
    "Template Upgrade Simulation (Windows)",
)

PASS_JOBS = frozenset({"success", "skipped"})
ACTIONS_APPS = frozenset({"github-actions", "github_actions", ""})


def jobs_ok(results: dict[str, str]) -> bool:
    """True when every needed Actions job is success or skipped."""
    if not results:
        return False
    return all((v or "").strip().lower() in PASS_JOBS for v in results.values())


def parse_job_pairs(items: Iterable[str]) -> dict[str, str]:
    out: dict[str, str] = {}
    for raw in items:
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise ValueError(f"expected name=result, got {raw!r}")
        name, result = line.split("=", 1)
        name = name.strip()
        if not name:
            raise ValueError(f"empty job name in {raw!r}")
        out[name] = result.strip().lower()
    return out


def _app_slug(run: dict) -> str:
    app = run.get("app")
    if isinstance(app, dict):
        return (app.get("slug") or "").strip().lower()
    return str(app or "").strip().lower()


def _is_success(run: dict) -> bool:
    conclusion = (run.get("conclusion") or "").strip().lower()
    state = (run.get("state") or "").strip().lower()
    return conclusion == "success" or state == "success"


def context_ok(
    runs: list[dict],
    name: str,
    statuses: list[dict] | None = None,
) -> bool:
    """True if a success check or commit status exists for name.

    Neutral-only rollups (GHAS CodeQL) do not count.
    """
    matching = [r for r in runs if (r.get("name") or "") == name]
    actions = [r for r in matching if _app_slug(r) in ACTIONS_APPS]
    if any(_is_success(r) for r in actions) or any(_is_success(r) for r in matching):
        return True
    for status in statuses or []:
        context = status.get("context") or status.get("name") or ""
        if context == name and _is_success(status):
            return True
    return False


def missing_required(
    runs: list[dict],
    required: tuple[str, ...] = REQUIRED_CONTEXTS,
    statuses: list[dict] | None = None,
) -> list[str]:
    return [n for n in required if not context_ok(runs, n, statuses)]


def rollup_ready(
    runs: list[dict],
    required: tuple[str, ...] = REQUIRED_CONTEXTS,
    statuses: list[dict] | None = None,
) -> bool:
    return not missing_required(runs, required, statuses)
