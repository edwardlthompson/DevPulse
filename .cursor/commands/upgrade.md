# Template upgrade (sim on this template; catch-up plan on a child)

Read @docs/UPGRADING_FROM_TEMPLATE.md and @docs/CURSOR_MODES.md. Other IDEs: `docs/help/UPGRADE.md`.

Decide repo mode with `is_template_repo` from `scripts/lib/build_sprint_model.py` (or the stamped project card / purpose in `bootstrap.config.json`).

## This template (maintainer)

```bash
python3 scripts/agent-run.py simulate-template-upgrade

```

If it fails, follow @docs/UPGRADING_FROM_TEMPLATE.md cherry-pick table and fix validate-bootstrap gaps before bumping `.template-version`.

## Child repo (catch-up — Plan only)

Do **not** edit the child. Produce a numbered gap list and a short plan with `### Critique`. Wait for named numbers. Monday cron may already have filled **Template gaps (synced)** on `BUILD_PLAN.md` via `sync-template-gaps-build-plan` — use that as the starting list.

1. Run `python3 scripts/agent-run.py check-template-updates` (or `pwsh scripts/check-template-updates.ps1`) and print the version banner.
2. Run `python3 scripts/agent-run.py check-template-gaps` (or read the synced BUILD_PLAN block) and classify rows:
   - **Canon** — safe to copy from upstream later
   - **Mixed** — merge; keep child values
   - **Sacred** — human only; never overwrite (`AGENTS.md`, `docs/spec.md`, `docs/plan.md`, live `.env`, `examples/`)
   - **Golden Path** — stack-filtered feature slices that are missing; adopt via `/feature` + `docs/features/{name}.md`, never overwrite the app with the stub
3. Write a Plan with mandatory `### Critique` (Issue→Resolution) for how to close the **named** gaps.
4. Offer: “Say the number(s) to apply.” Canon → `[AGENT]` copy; Mixed → `[AGENT]` merge + `[HUMAN]` review; Sacred → `[HUMAN]` only; Golden Path → one `[AGENT]` `/feature` row each.
5. **No silent `do all`.** If the user says `do all`, `implement all`, `add all`, or `yes to everything`, do not add rows and do not copy files. Restate the numbered list and wait until they name specific numbers.

Begin now.
