"""Attempt BUILD_PLAN HUMAN/ADB rows via scripts and automation."""
from __future__ import annotations

import json
import re
from pathlib import Path

from human_task_adb_device import (
    automate_density_font_scale,
    automate_display_mode,
    automate_foldable_multiwindow,
    automate_talkback_checklist,
    automate_theme_process_death,
    automate_unifiedpush_e2e,
)
from human_task_android import (
    automate_adb_instrumented,
    automate_android_sdk_smoke,
    automate_fdroid_dry_run,
)
from human_task_core import AttemptResult, resolve_config
from human_task_github import (
    automate_automerge_token,
    automate_branch_protection,
    automate_dependabot_major_merge,
)
from human_task_leftovers import (
    automate_cii_badge,
    automate_codeowners_about,
    automate_crash_proxy_off,
    automate_dependabot_weekly,
    automate_mcp_copy,
    automate_ollama,
    automate_scorecard_badge,
)
from human_task_rows import (
    automate_approve_adr,
    automate_informational,
    automate_init_placeholders,
    automate_product_smoke,
    automate_release_tag,
    automate_stack_config,
    automate_use_template,
)
from human_task_release_please import automate_release_please_merge
from human_task_waiting_docs import (
    automate_lightroom_smoke,
    automate_openssf_gap_list,
    automate_winget_checklist,
)
from human_task_waiting_gh import (
    automate_pages_custom_domain,
    automate_private_vuln_reporting,
    automate_push_protection,
)

HUMAN_RULES: list[tuple[re.Pattern[str], str, object]] = [
    (re.compile(r"Use this template", re.I), "human", automate_use_template),
    (re.compile(r"Fill placeholders.*INITIALIZATION_PROMPT", re.I), "human", automate_init_placeholders),
    (re.compile(r"Pick Cursor mode", re.I), "human", lambda r, c: automate_informational(r, c, "cursor-mode")),
    (re.compile(r"Bookmark.*BATCH_COMMANDS", re.I), "human", lambda r, c: automate_informational(r, c, "bookmark-commands")),
    (re.compile(r"Fill stack-local config|app-update\.json", re.I), "human", automate_stack_config),
    (re.compile(r"Approve ADR|Approve.*BUILD_PLAN", re.I), "human", automate_approve_adr),
    (re.compile(r"Optional product smoke", re.I), "human", automate_product_smoke),
    (re.compile(r"Approve release tag", re.I), "human", automate_release_tag),
    (re.compile(r"required status checks|branch protection|setup-github-repo", re.I), "human", automate_branch_protection),
    (re.compile(r"Dependabot PR|Review/merge Dependabot|TypeScript \d+ major", re.I), "human", automate_dependabot_major_merge),
    (re.compile(r"AUTOMERGE_TOKEN", re.I), "human", automate_automerge_token),
    (re.compile(r"Scorecard badge", re.I), "human", automate_scorecard_badge),
    (re.compile(r"CII Best Practices", re.I), "human", automate_cii_badge),
    (re.compile(r"Ollama", re.I), "human", automate_ollama),
    (re.compile(r"Crash-proxy|DPIA", re.I), "human", automate_crash_proxy_off),
    (re.compile(r"mcp\.foss\.example|mcp\.json", re.I), "human", automate_mcp_copy),
    (re.compile(r"Dependabot interval|disable automerge", re.I), "human", automate_dependabot_weekly),
    (re.compile(r"CODEOWNERS|Watch repo Issues", re.I), "human", automate_codeowners_about),
    (re.compile(r"Release Please|Approve/merge Release", re.I), "human", automate_release_please_merge),
    (re.compile(r"Lightroom", re.I), "human", automate_lightroom_smoke),
    (re.compile(r"Private vulnerability reporting", re.I), "human", automate_private_vuln_reporting),
    (re.compile(r"Baseline-1|OpenSSF gap", re.I), "human", automate_openssf_gap_list),
    (re.compile(r"Winget submission checklist", re.I), "human", automate_winget_checklist),
    (re.compile(r"Secret scanning push protection", re.I), "human", automate_push_protection),
    (re.compile(r"Pages custom domain", re.I), "human", automate_pages_custom_domain),
]

ADB_RULES: list[tuple[re.Pattern[str], str, object]] = [
    (re.compile(r"TalkBack", re.I), "adb", automate_talkback_checklist),
    (re.compile(r"UnifiedPush", re.I), "adb", automate_unifiedpush_e2e),
    (re.compile(r"display mode|Preferred display", re.I), "adb", automate_display_mode),
    (re.compile(r"foldable|multi-window", re.I), "adb", automate_foldable_multiwindow),
    (re.compile(r"Theme change persists|process death", re.I), "adb", automate_theme_process_death),
    (re.compile(r"density|font-scale", re.I), "adb", automate_density_font_scale),
    (re.compile(r"instrumented|connectedDebugAndroidTest|\badb\b|nav smoke|Golden Path nav", re.I), "adb", automate_adb_instrumented),
    (re.compile(r"F-Droid|device dry-run", re.I), "adb", automate_fdroid_dry_run),
    (re.compile(r"emulator|Android SDK", re.I), "adb", automate_android_sdk_smoke),
]


def attempt_row(root: Path, owner: str, task: str, sprint: str) -> AttemptResult:
    cfg = resolve_config(root)
    owner_u = owner.upper()
    rules = HUMAN_RULES if owner_u == "HUMAN" else ADB_RULES if owner_u == "ADB" else []
    for pattern, _kind, handler in rules:
        if not pattern.search(task):
            continue
        if handler is automate_approve_adr:
            return handler(root, cfg, task)
        return handler(root, cfg)  # type: ignore[operator]
    return AttemptResult(1, "no-match", f"No automation rule for {owner} task in sprint {sprint}", True)


def main() -> int:
    import argparse

    from build_backlog import remove_item

    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--owner", required=True)
    parser.add_argument("--task", required=True)
    parser.add_argument("--sprint", default="")
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    result = attempt_row(root, args.owner, args.task, args.sprint)
    if result.exit_code == 0 and args.sprint:
        remove_item(root, args.owner, args.task, args.sprint)
    if args.json:
        print(json.dumps(result.to_dict(), indent=2))
    else:
        print(f"{result.method}: exit={result.exit_code} {result.reason}")
    return result.exit_code


if __name__ == "__main__":
    raise SystemExit(main())
