"""Attempt BUILD_PLAN HUMAN/ADB rows via scripts and automation."""
from __future__ import annotations

import json
import re
from pathlib import Path

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
from human_task_rows import (
    automate_approve_adr,
    automate_informational,
    automate_init_placeholders,
    automate_product_smoke,
    automate_release_tag,
    automate_stack_config,
    automate_use_template,
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
]

ADB_RULES: list[tuple[re.Pattern[str], str, object]] = [
    (re.compile(r"instrumented|connectedDebugAndroidTest|\badb\b", re.I), "adb", automate_adb_instrumented),
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

    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--owner", required=True)
    parser.add_argument("--task", required=True)
    parser.add_argument("--sprint", default="")
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()
    result = attempt_row(Path(args.root).resolve(), args.owner, args.task, args.sprint)
    if args.json:
        print(json.dumps(result.to_dict(), indent=2))
    else:
        print(f"{result.method}: exit={result.exit_code} {result.reason}")
    return result.exit_code


if __name__ == "__main__":
    raise SystemExit(main())
