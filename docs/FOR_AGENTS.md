# For Agents

## Phased Loading

SessionStart -> START_HERE.md -> CURSOR_MODES (pick mode) -> AGENTS.md -> docs/spec.md + docs/plan.md -> BUILD_PLAN Sequential -> Active module -> WEB_PROJECT_LAYOUT (web hosting) -> DESIGN_GUIDE (web/android UI) -> Plan or Agent

Batch slash commands: see [`docs/BATCH_COMMANDS.md`](BATCH_COMMANDS.md) (humans: [`docs/help/BATCH_COMMANDS.md`](help/BATCH_COMMANDS.md)).

## Cursor mode transitions

See [`docs/CURSOR_MODES.md`](CURSOR_MODES.md) for the full table. Key triggers:

- **Agent → Plan** — shared schema change, scope expanded, or file outside feature container
- **Agent → Debug** — gate exit 1 after autofix exhausted, CI red, or flaky repro
- **Debug → Agent** — root cause confirmed and fix approach agreed
- **Plan → Agent** — plan approved ("execute the plan")

Do not debug in Plan Mode. Do not edit in Ask Mode.

## Token Economy

1. Never read all of `examples/` - active stack only
2. Never fill KNOWLEDGE_BASE.md with generic framework docs
3. Update memory files only at session start, milestone end, or architectural pivot
4. Read-before-write: @filename before edits
5. Sequential before Parallel in BUILD_PLAN
6. Respect `.cursorignore` — do not read `node_modules/`, `dist/`, or other ephemeral paths

File size budgets: **300 lines static data** (UI + i18n), **150 lines pure logic** — see [`docs/FILE_SIZE_GUIDE.md`](FILE_SIZE_GUIDE.md).

## BUILD_PLAN status markers

Every task row in `BUILD_PLAN.md` and checklist in module docs, PR template, and feature specs uses emoji status (readable in source and Preview):

| Marker | State |
|--------|-------|
| 🔲 | Open — default for new tasks |
| ✅ | Done — swap 🔲 when complete; archive sprint to `COMPLETED_TASKS.md` |
| ❌ | Blocked — swap 🔲 and append reason |

**Format:** `🔲 Description` (or `🔲 [OWNER] Description` on BUILD_PLAN) · do not use `- [ ]` GitHub checkboxes.

## Repo hygiene

- Track source and lockfiles only; never commit build output or caches (`docs/REPO_HYGIENE.md`)
- Before push: `bash scripts/check-repo-hygiene.sh`
- Reclaim disk: `bash scripts/purge-ephemeral.sh` (dry-run); `--apply` removes gitignored untracked files only
- Stage explicit paths; avoid blind `git add -A`

## Parallel-first planning and dispatch

**Planning (Plan Mode):** Include `### Parallelization` with decomposition table and `agent_count_target`. Run `check-build-plan-parallel.sh` before human approval. See BUILD_PLAN decomposition checklist.

**Execution (orchestrator):**

```bash
bash scripts/plan-parallel-dispatch.sh --require-sequential-clear --json
bash scripts/check-parallel-scope.sh
```

Write manifest to `.cursor/parallel-scope-lock.json`. Run `/scope` for auto Task dispatch when `agent_count >= 2`.

- Branch: `feature/agent-[task-name]` per agent; optional `scripts/setup-agent-worktrees.sh`
- Shared schema/types: sequential agent only first
- Parallel agents **never** edit `BUILD_PLAN.md` or composition roots
- Subagents report: `bash scripts/agent-progress.sh set-step --name tests|view|e2e`
- Scope map: `docs/PARALLEL_AGENT_SCOPES.md`

## Autonomous `/build` — HUMAN/ADB automation

`/build` runs all `[AGENT]`/`[AUTO]` and Parallel work first, then attempts the grouped **Human & device (after automation)** section via `scripts/attempt-build-plan-row.sh`. Never halts on human labels.

| Phase | Action |
|-------|--------|
| Sequential + Parallel + post-merge AGENT | `execute` or `parallel_dispatch` |
| Human & device (after automation) | `automate_human` / `automate_adb` |
| Automation exit 0 | Mark row ✅ in BUILD_PLAN |
| Automation exit 1 | `scripts/build-backlog.sh add` → continue (row stays open in human group) |

Place `[HUMAN]`/`[ADB]` rows only under `#### Human & device (after automation)` (or `### Human (after automation)` in maintenance) so humans can address them as one block after automation.

Config env vars (optional): `BUILD_STACK`, `BUILD_PROJECT_NAME`, `BUILD_PURPOSE`, `GITHUB_REPO`, `BUILD_DONATION_URL`. Fallback: `.cursor/stack-selection.json`, `gh repo view`, folder name.

Status: `bash scripts/build-sprint-status.sh --json --lane child` → `next_row.action` is `automate_human`, `automate_adb`, `execute`, or `parallel_dispatch`. Rows already in `HUMAN_BACKLOG.md` are skipped until a human clears them.

Disable autonomous ADR approval in child repos: add `<!-- no-auto-approve -->` to BUILD_PLAN.

## 3-Strike Rule

After 3 failed fix attempts: halt, summarize conflict, request human direction. Switch to **Debug Mode** per [`docs/CURSOR_MODES.md`](CURSOR_MODES.md) when root cause is unclear.

Do not loop on the same file with identical errors. Escalate with:
- Failing command output (last attempt only)
- Files touched
- Proposed next options for human pick

## Session Checkpoint

1. Copy `.cursor-session-state.example.json` to `.cursor-session-state.json`
2. Fill `mode` (repo mode: bootstrap|reference — not Cursor Ask/Plan/Agent/Debug), `stack`, `active_sprint`, `sequential_step`, `last_files_touched`, `current_feature`, `strikes`
3. Clear chat; on restart read the state file, pick Cursor mode via [`docs/CURSOR_MODES.md`](CURSOR_MODES.md), then resume BUILD_PLAN Parallel lane
4. Cross-check `.cursor/agent-progress.json` (written by `watch-agent-gates.sh`)
5. Delete `.cursor-session-state.json` after successful restore

Stack selection from init lives in `.cursor/stack-selection.json`.

## Autonomous feature gates (Sprint 2+)

After each `[AGENT]` BUILD_PLAN step in a feature row:

```bash
bash scripts/watch-agent-gates.sh --once --autofix
```

- Exit `0`: proceed to next step
- Exit `1`: read gate JSON stdout + `.cursor/agent-progress.json`; auto-fix lint/tests in active feature container + wiring + tests; re-run until pass
- Exit `2`: environment block or 3-strike — halt and escalate to human

Extended sessions:

```bash
bash scripts/watch-agent-gates.sh --interval 60 --max-attempts 10 --autofix
```

Mechanical fixers run first via `feature-autofix.sh`. Push to remote still requires human approval (`destructive-ops.mdc`).

See `docs/FEATURE_MODULES.md`.

## Failure Playbook

Use **Debug Mode** (`docs/CURSOR_MODES.md`, PROMPT_LIBRARY Entry 20) when CI or local gates fail and root cause is unclear.

### CI poll after push

```bash
bash scripts/check-github-ci.sh --wait 300
# Windows: pwsh scripts/check-github-ci.ps1 -WaitSeconds 300
```

Required green workflows: **CI**, **Security Scan**, **CodeQL**.

If a job is missing, wait - GitHub may not have enqueued it yet. If `FAIL` persists:
1. Open the run URL from script output
2. Fix locally; re-run `validate-bootstrap.sh` before pushing again
3. Do not mark BUILD_PLAN `[AUTO]` items complete while red

### GH_TOKEN / gh CLI

- `validate-workflow-actions.sh` and `check-github-ci.sh` need `gh auth login`
- In CI, `GITHUB_TOKEN` is injected automatically; locally export `GH_TOKEN` if using a PAT
- `gh: HTTP 401` -> re-authenticate; `404` -> confirm repo remote and `gh repo set-default`

### Dependabot conflicts

1. Triage Critical/High first (`docs/SECURITY_TRIAGE.md`)
2. For conflicting lockfile PRs: checkout branch, `npm ci` / `uv sync --locked`, run tests, push
3. Transitive CVEs without direct bump: see KNOWLEDGE_BASE KB-007 overrides policy
4. Never merge with failing **dependency-review** on PRs

### Parallel scope collision

Before launching parallel agents:

```bash
bash scripts/check-parallel-scope.sh
```

If overlap is reported, split tasks or serialize the conflicting rows in BUILD_PLAN.

### Encoding failures on Windows

Run `python3 scripts/check-file-encoding.py` after edits. Write text with UTF-8 (no BOM); never UTF-16.
