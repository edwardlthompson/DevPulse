# Autonomous sprint build (end-to-end, automate HUMAN/ADB first)

Execute the BUILD_PLAN **without asking the user questions, presenting options, or waiting for approval.** Pick the single best path internally (### Critique + ### Parallelization in your reasoning only), then implement it.

**Goal:** Complete as much of BUILD_PLAN as possible in one run. Run all `[AGENT]`/`[AUTO]` and Parallel work first, then attempt the grouped **Human & device (after automation)** section. Chain sprints until all actionable rows are done, 3-strike failure, or environment block.

## Rules

- **Never stop** to ask "which approach?" — decide and execute.
- **Never stop** for plan approval — `/build` is self-approving.
- **Never halt** on `[HUMAN]` or `[ADB]` labels — automate first; backlog only on failure.
- **Never stop** after a single feature row if the active sprint still has open work.
- **Do stop** only when: (a) 3-strike gate failure, (b) environment block (exit 2) after autofix, or (c) all actionable rows complete (`all_sprints_agent_auto_complete`).
- On gate failure: run @.cursor/commands/fix.md autonomously (up to 3 strikes) — do not suggest `/fix` and wait.

## Step 0 — Load sprint state

Use `--lane auto` (not `child`): on this template it selects the maintainer board; on child repos it walks `BUILD_PLAN.md` (installed from `BUILD_PLAN_TEMPLATE.md`).

```bash
python3 scripts/agent-run.py build-sprint-status --json --lane auto

```

`auto` uses the product board on child repos and the Template Maintainer board on this template. Pass `--lane child` to walk `BUILD_PLAN_TEMPLATE.md` on this repo (Sprint 0+).

Write `.cursor-session-state.json` fields: `active_sprint`, `build_plan_lane`, `autonomous_mode: true`.

**Open PRs sync (once):** if `gh` is available, run `python3 scripts/agent-run.py sync-open-prs-build-plan -- --check`. On exit **0**, print one line (`Open PRs sync current`) and **do not** `--apply` — avoids rewriting an already-✅ / empty Open PRs block every loop. Only run `--apply` when `--check` reports stale. Do **not** re-sync on every Step 1a iteration.

If `all_sprints_agent_auto_complete: true` → print summary (include `HUMAN_BACKLOG.md` path if items exist) and exit.

## Step 1 — Sprint execution loop

Repeat until `sprint_agent_auto_complete`:

### 1a. Read status

```bash
python3 scripts/agent-run.py build-sprint-status --json --lane auto

```

- If `next_row` is null and `sprint_agent_auto_complete` → go to Step 2 (sprint wrap-up).

### 1b. Execute `next_row`

| `next_row.action` | Action |
|-------------------|--------|
| `automate_human` / `automate_adb` | Run `python3 scripts/agent-run.py attempt-build-plan-row --owner "<owner>" --task "<task>" --sprint "<sprint>" --json`. On exit 0: replace 🔲 → ✅ for that row in BUILD_PLAN.md. On exit 1: `python3 scripts/agent-run.py build-backlog add --owner "<owner>" --task "<task>" --sprint "<sprint>" --reason "<reason from JSON>"` and **continue** (row stays open). |
| `execute` | Implement the task; for post-Parallel step 3 skip if `parallel_steps_completed` includes `tests`; for step 4 skip if includes `view`; gate; mark ✅ |
| `parallel_dispatch` | Run @.cursor/commands/scope.md fully, then `python3 scripts/agent-run.py agent-progress set-parallel-sprint-done --sprint "<sprint title>"` |
| AUTO rows | Run listed scripts/commands to completion; mark ✅ on exit 0 |
### 1c. Gate autofix (every AGENT step)

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto

```

`--scope auto` gates only dirty stacks (git vs `HEAD`: `examples/{stack}/`, or preamble-only for docs/changelog/commands). Shared `scripts/` / `schemas/` / `modules/` still run the full multi-stack gate. Print the `gate scope:` line. Override with `--scope full` or `FEATURE_GATE_SCOPE=full`.

Exit 1 → fix in scope and re-run (3-strike max). Exit 2 after 3 strikes → halt with evidence; do not ask user.

Skip gates for successful `automate_human`/`automate_adb` no-op/informational steps unless the automation script ran product smoke or init.

### 1d. Loop

**Gate lock:** do not execute another `next_row` until step 1c `watch-agent-gates` exited 0. Never skip 1c to chain a second feature.

Re-run `build-sprint-status.sh --json` and continue 1a.

## Step 2 — Sprint wrap-up

When `sprint_agent_auto_complete` for current sprint:

1. `python3 scripts/agent-run.py smoke-sprint --require --sprint "<sprint title>"` — re-smoke **every** ✅ row (no errors/crashes; startup + load order). Exit ≠ 0 → do **not** mark the sprint done or chain to the next sprint; leave the last row 🔲/❌ and `/fix`.
2. @.cursor/commands/gates.md — full local validation (includes `smoke-sprint --if-complete`)
3. @.cursor/commands/cleanup.md — archive ✅ rows (including auto-completed HUMAN/ADB); backlog items stay open on board
4. Print brief summary: sprint name, rows completed, rows automated, rows backlogged (`HUMAN_BACKLOG.md`), and pointer to grouped **Human & device (after automation)** section for manual follow-up

## Step 3 — Chain to next sprint

Re-run `python3 scripts/agent-run.py build-sprint-status --json --lane auto`.

- If `next_row` exists → **go to Step 1** immediately (no user pause).
- If `all_sprints_agent_auto_complete` → print final summary: all actionable BUILD_PLAN work complete; list `backlogged_human_adb` and `HUMAN_BACKLOG.md` path.

## Progress logging

Log one line per completed row (owner + task + automated|implemented). Do not dump full plans between rows. User sees sprint summaries only at wrap-up and final exit.

Begin now. Do not ask the user anything until the loop exits.
