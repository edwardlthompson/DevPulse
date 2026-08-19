# Batch Commands — Agent Registry

> Technical catalog for agents and maintainers. **Humans:** start with [docs/help/BATCH_COMMANDS.md](help/BATCH_COMMANDS.md).

30 slash commands: **25 atomic** workflows + **5 super** orchestrators. Bare-word triggers: `.cursor/rules/batch-commands.mdc`. Other IDEs: paste the matching file under `docs/help/` (start with `docs/help/TOUR.md` or `docs/help/IDEAS.md`).

## Super commands

| Command | Chain | Cursor mode | PROMPT_LIBRARY | Push? |
|---------|-------|-------------|----------------|-------|
| `/bootstrap` | init → prune → setup → gates | Agent | 42 | No |
| `/verify` | docs → gates → ci | Agent | 43 | No |
| `/build` | Autonomous BUILD_PLAN sprint chain — automates HUMAN/ADB first, backlog on failure | Agent | 44 | No |
| `/ship` | prerelease (autofix + optional Codex + hard gate) → push → regress | Agent | 45 | **Yes** |
| `/maintain` | triage → dependabot → audit | Agent | 46 | No |
## Atomic commands

| Command | Workflow | Super parent | PROMPT_LIBRARY |
|---------|----------|--------------|----------------|
| `/audit` | Full repo review → BUILD_PLAN → execute → cleanup | maintain | 22 |
| `/codex-review` | Optional Codex read-only review → CODE_REVIEW.md → BUILD_PLAN + `/fix` | prerelease, ship | — |
| `/cleanup` | Archive ✅ BUILD_PLAN rows → COMPLETED_TASKS.md | build, audit, push, init | — |
| `/debug` | Defect investigation | — | 20 |
| `/gates` | Local validation suite | bootstrap, verify, build | 4/5 |
| `/triage` | Weekly security pass | maintain | 6 |
| `/dependabot` | Triage/merge Dependabot; KB-007 overrides | maintain | 6 + KB-007 |
| `/push` | Release commit → push → release | ship | 26 |
| `/prerelease` | `pre-release-gate.sh`; zero Critical/High | ship | 3/10 |
| `/regress` | Post-release SBOM, Pages, upgrade sim | ship | 15 |
| `/feature` | Sprint 2+ vertical slice + gate loop | build | 17 |
| `/fix` | `watch-agent-gates --autofix` in feature scope | build | 17 |
| `/init` | Sprint 0 bootstrap | bootstrap | 1 |
| `/prune` | Verify stack selection + pruned examples | bootstrap | 12 |
| `/ci` | Post-push CI poll only | verify | 9 |
| `/docs` | README health + markdown tables + encoding | verify | 5 |
| `/upgrade` | Template upgrade sim | maintain | 16 |
| `/setup` | GitHub repo settings | bootstrap | 11 |
| `/plan` | Feature/ADR plan + resolved Critique (Issue→Resolution) | build | 19 |
| `/restore` | Restore from `.cursor-session-state.json` | — | 13 |
| `/compact` | Save session state before clearing chat | — | 13 |
| `/scope` | Parallel manifest + auto Task dispatch | — | 14 |
| `/coach` | Project health + next action + industry why (BEST_PRACTICES) | bootstrap | — |
| `/tour` | 10-minute first-run walk (START_HERE → why → Golden Path → Week 1) | bootstrap | — |
| `/ideas` | Ranked in-scope backlog (do not implement; offer BUILD_PLAN rows) | — | — |
## Decision tree

```
New repo?           → /bootstrap
Changed code?       → /verify (or /docs if docs-only)
What next (now)?    → /coach
What could we add?  → /ideas
New feature?        → /build  (or /fix if gates fail)
Ready to publish?   → /ship   (or /prerelease then /push)
Weekly maintenance? → /maintain (heavy) or /triage + /verify (light)
Bug with evidence?  → /debug  (not /audit)
Long chat session?  → /compact before clear · /restore after

```

## `/verify` vs `/gates` vs `/push` vs `/ship`

| Command | Scope |
|---------|-------|
| `/gates` | Local scripts only — no CI poll. **bootstrap-doctor** alias: `validate-bootstrap --quick` or `run-maintainer-gates` |
| `/verify` | docs + gates + CI (pre-merge) |
| `/push` | Full release workflow with explicit push approval |
| `/ship` | prerelease + push + regress (preferred publish path) |
## File layout

| Path | Role |
|------|------|
| `.cursor/commands/*.md` | Slash command bodies (loaded on `/name`) |
| `.cursor/rules/batch-commands.mdc` | Bare-word → same files |
| `docs/help/BATCH_COMMANDS.md` | Human cheat sheet |
| `CODE_REVIEW.md.example` | Audit output template |
| `RELEASE_NOTES.md.example` | Release draft template |
| `scratchpad.md.example` | Phase working memory (live `scratchpad.md` gitignored) |
| `docs/features/_handoff.md` | Parallel-agent handoff stub |
| `scripts/check-batch-commands.sh` | Registry ↔ filesystem sync |
Validation: `bash scripts/check-batch-commands.sh` (also via `validate-bootstrap.sh --quick`).
