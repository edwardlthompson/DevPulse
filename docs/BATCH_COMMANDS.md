# Batch Commands — Agent Registry

> Technical catalog for agents and maintainers. **Humans:** start with [docs/help/BATCH_COMMANDS.md](help/BATCH_COMMANDS.md).

36 slash commands: **31 atomic** workflows + **5 super** orchestrators. Bare-word triggers: `.cursor/rules/batch-commands.mdc`. Other IDEs: paste the matching file under `docs/help/` (start with `docs/help/TOUR.md`, `docs/help/IDEAS.md`, or `docs/help/ALLIDEAS.md`). Novice print sheet: [`docs/help/batch-commands-print.html`](help/batch-commands-print.html).

## Super commands

| Command | Chain | Cursor mode | PROMPT_LIBRARY | Push? |
|---------|-------|-------------|----------------|-------|
| `/bootstrap` | init → prune → setup → gates | Agent | 42 | No |
| `/verify` | docs → gates → ci | Agent | 43 | No |
| `/build` | Autonomous BUILD_PLAN sprint chain — per-row `--scope auto` gates; wrap-up `smoke-sprint --require` then `/gates` | Agent | 44 | No |
| `/ship` | update-deps → prerelease (`--local`) → push → regress | Agent | 45 | **Yes** |
| `/maintain` | triage → update-deps → dependabot → audit | Agent | 46 | No |
## Atomic commands

| Command | Workflow | Super parent | PROMPT_LIBRARY |
|---------|----------|--------------|----------------|
| `/audit` | Full repo review → BUILD_PLAN → execute → cleanup | maintain | 22 |
| `/codex-review` | Advanced/optional read-only review → CODE_REVIEW.md → BUILD_PLAN + `/fix` (not first-time; not `/ship`) | — | — |
| `/cleanup` | Archive ✅ BUILD_PLAN rows → COMPLETED_TASKS.md | build, audit, push, init | — |
| `/debug` | Defect investigation | — | 20 |
| `/gates` | Local validation suite (always canvas status overview) | bootstrap, verify, build | 4/5 |
| `/triage` | Weekly security pass | maintain | 6 |
| `/dependabot` | Leftover GitHub Dependabot alerts/PRs after local apply | maintain | 6 + KB-007 |
| `/push` | Release commit → push → release | ship | 26 |
| `/prerelease` | `pre-release-gate.sh --local`; local CVE audit | ship | 3/10 |
| `/update-deps` | Local dry-run/audit/apply (depsonar MCP or upd CLI) | ship, maintain | — |
| `/best-of-n` | Local worktree model race (cap N by RAM; never push) | — | — |
| `/emulator` | Local AOSP instrumented tests (skip if no SDK) | — | — |
| `/regress` | Post-release SBOM, Pages, upgrade sim | ship | 15 |
| `/feature` | Sprint 2+ vertical slice + gate loop | build | 17 |
| `/fix` | `watch-agent-gates --autofix` in feature scope | build | 17 |
| `/init` | Sprint 0 bootstrap | bootstrap | 1 |
| `/prune` | Verify stack selection + pruned examples | bootstrap | 12 |
| `/ci` | Post-push CI poll only | verify | 9 |
| `/docs` | README health + markdown tables + encoding | verify | 5 |
| `/upgrade` | Child: template gap plan (Canon/Mixed/Sacred + Golden Path). Template: upgrade sim | maintain | 16 |
| `/setup` | GitHub repo settings | bootstrap | 11 |
| `/plan` | Feature/ADR plan + resolved Critique (Issue→Resolution) | build | 19 |
| `/adr` | Write the next numbered `docs/adr/` record | — | — |
| `/restore` | Restore from `.cursor-session-state.json` | — | 13 |
| `/compact` | Save session state before clearing chat | — | 13 |
| `/resume` | After Cloud Agent: fetch + sync open Dependabot/release PRs + list `cursor/*` PRs + next AGENT row | — | — |
| `/scope` | Parallel manifest + auto Task dispatch | — | 14 |
| `/coach` | Project health + next action + industry why (BEST_PRACTICES) | bootstrap | — |
| `/tour` | 10-minute first-run walk (START_HERE → why → Golden Path → Week 1) | bootstrap | — |
| `/ideas` | Ranked in-scope backlog (do not implement; offer BUILD_PLAN rows) | — | — |
| `/allideas` | Uncapped in-scope dump for BUILD_PLAN fill (do not implement until asked) | — | — |
## Decision tree

```
New repo?           → /bootstrap
Changed code?       → /verify (or /docs if docs-only)
What next (now)?    → /coach
What could we add?  → /ideas (ranked 5–8) or /allideas (complete dump)
New feature?        → /build  (or /fix if gates fail)
Ready to publish?   → /ship   (or /prerelease then /push)
Weekly maintenance? → /maintain (heavy) or /triage + /update-deps (light)
Template catch-up?   → /upgrade (child: plan only; this template: sim)
Bug with evidence?  → /debug  (not /audit)
Long chat session?  → /compact before clear · /restore after
Back from Cloud?    → /resume (fetch + open PRs + next AGENT row)

```

## `/verify` vs `/gates` vs `/push` vs `/ship`

| Command | Scope |
|---------|-------|
| `/gates` | Local scripts only — no CI poll. Always render canvas-bootstrap-status. **bootstrap-doctor** alias: `validate-bootstrap --quick` or `run-maintainer-gates` |
| `/verify` | docs + gates + CI (pre-merge) |
| `/push` | Full release workflow with explicit push approval |
| `/ship` | update-deps + local prerelease + push + regress (preferred publish path) |
## File layout

| Path | Role |
|------|------|
| `.cursor/commands/*.md` | Slash command bodies (loaded on `/name`) |
| `.cursor/rules/batch-commands.mdc` | Bare-word → same files |
| `docs/help/BATCH_COMMANDS.md` | Human cheat sheet |
| `docs/help/batch-commands-print.html` | Novice print sheet (browser Print) |
| `docs/help/UPGRADE.md` | Child catch-up recipe (other IDEs) |
| `CODE_REVIEW.md.example` | Audit output template |
| `RELEASE_NOTES.md.example` | Release draft template |
| `scratchpad.md.example` | Phase working memory (live `scratchpad.md` gitignored) |
| `docs/features/_handoff.md` | Parallel-agent handoff stub |
| `scripts/check-batch-commands.sh` | Registry ↔ filesystem sync |
Validation: `bash scripts/check-batch-commands.sh` (also via `validate-bootstrap.sh --quick`).

## `/allideas` → board → `/build` recipe

1. `/allideas` (or `docs/help/ALLIDEAS.md`) — dump in-scope ideas; do not implement yet.
2. Say `board` / `add all` / name numbers — agent adds 🔲 `[AGENT]` rows to `BUILD_PLAN.md` (cap per sprint; archive prior milestone first).
3. `/build` — autonomous execution with `--scope auto` gates; HUMAN/ADB attempted after AGENT/AUTO, failures → `HUMAN_BACKLOG.md`.
4. Sprint wrap: `smoke-sprint --require` → `/gates` → `/cleanup` archive.

Human cheat sheet: [`docs/help/BATCH_COMMANDS.md`](help/BATCH_COMMANDS.md).
