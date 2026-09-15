"""Format Cloud → PC resume digests."""
from __future__ import annotations

from pathlib import Path
from typing import Any

from gates_canvas import next_open_row
from health_ci import ci_red_one_liner
from health_notes import unreleased_has_entries
from sync_open_prs_render import classify_pr


def cursor_prs(prs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    out: list[dict[str, Any]] = []
    for pr in prs:
        if classify_pr(pr):
            continue
        if str(pr.get("headRefName") or "").startswith("cursor/"):
            out.append(pr)
    return out


def format_digest(
    root: Path,
    *,
    sync_inner: str | None,
    synced_prs: list[dict[str, Any]],
    cloud_prs: list[dict[str, Any]],
    gh_error: str | None,
    fetch_note: str,
    branch_notes: list[str],
    ci_line: str | None = None,
) -> str:
    dirty_unreleased = unreleased_has_entries(root)
    lines = [
        "# Resume handoff (Cloud -> PC)",
        "",
        f"- git fetch: {fetch_note}",
    ]
    for note in branch_notes:
        lines.append(f"- {note}")
    lines.append(
        f"- CHANGELOG [Unreleased] has entries: "
        f"{'yes' if dirty_unreleased else 'no'}"
    )
    if ci_line is None:
        ci_line = ci_red_one_liner(root) if gh_error is None else f"CI: skipped ({gh_error})"
    lines.append(f"- {ci_line}")
    if dirty_unreleased or (ci_line and "CI red" in ci_line):
        lines.append(
            "- Handoff: dirty Unreleased and/or CI red — fix or /ship before filling ideas."
        )
    lines.append(f"- Next BUILD_PLAN row: {next_open_row(root)}")
    lines.append("")
    lines.append("## Open Dependabot / Release Please")
    if gh_error:
        lines.append(f"_gh unavailable: {gh_error}_")
    elif not synced_prs:
        lines.append("_None open._")
    else:
        for pr in synced_prs:
            kind = classify_pr(pr) or "?"
            lines.append(f"- #{pr.get('number')} ({kind}) {pr.get('title')} — {pr.get('url')}")
    if sync_inner:
        lines.extend(["", "BUILD_PLAN synced block:", "```", sync_inner, "```"])
    lines.append("")
    lines.append("## Open Cloud (`cursor/*`) PRs")
    if gh_error:
        lines.append(f"_Skipped ({gh_error})_")
    elif not cloud_prs:
        lines.append("_None open._")
    else:
        for pr in cloud_prs:
            lines.append(
                f"- #{pr.get('number')} {pr.get('title')} — {pr.get('url')} "
                f"(`{pr.get('headRefName')}`)"
            )
        lines.append("")
        lines.append(
            "Merge or close Cloud PRs before starting a new AGENT feature row when they conflict."
        )
    lines.extend(
        [
            "",
            "## Next",
            "Continue from the next open [AGENT] row above, or merge listed open PRs first.",
            "Do not rely on /compact session state across Cloud <-> PC (it is gitignored).",
            "",
        ]
    )
    return "\n".join(lines)
