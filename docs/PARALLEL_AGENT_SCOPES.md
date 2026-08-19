# Parallel Agent Scopes

> Isolated file scopes for BUILD_PLAN Parallel lane. No two agents may touch the same path prefix.

## Rules

1. One branch per agent: `feature/agent-<task-slug>`
2. Run `bash scripts/plan-parallel-dispatch.sh` then `bash scripts/check-parallel-scope.sh` before dispatch
3. Orchestrator writes `.cursor/parallel-scope-lock.json` from manifest; subagents read it first
4. Shared types/schemas: **Sequential agent only**
5. Never edit `BUILD_PLAN.md` from parallel agents (sequential owner)
6. **Forbidden paths:** `BUILD_PLAN.md`, `COMPLETED_TASKS.md`, `appBootstrap.ts`, `GoldenPathApp.kt`, `main.ts`
7. Optional handoff: copy [`docs/features/_handoff.md`](features/_handoff.md) → gitignored `.cursor/handoff-<scope>.md` (from/to, scope prefix, acceptance). Do not replace the scope lock or invent a second task board.

## Automatic dispatch

1. Orchestrator completes Sequential schema-lock steps
2. `bash scripts/plan-parallel-dispatch.sh --json` → **agent_count**
3. `/scope` launches Task subagents when count ≥ 2 (see `.cursor/commands/scope.md`)
4. Optional hard isolation: `bash scripts/setup-agent-worktrees.sh` (creates `.cursor/worktrees/`)

## Native Cursor worktrees

Cursor Agents Window, IDE `/worktree`, `/best-of-n`, and CLI read [`.cursor/worktrees.json`](../.cursor/worktrees.json). Setup scripts (fail-soft):

- `.cursor/setup-worktree-unix.sh`
- `.cursor/setup-worktree-windows.ps1`

They install stack deps when tools exist, copy only `*.env.example` (never `.env`), and exit **0** when stack/tools are missing so worktree creation does not fail.

| Isolation | When |
|-----------|------|
| Native `/worktree` or `/best-of-n` | Single-task or multi-model experiments without touching the main checkout |
| `setup-agent-worktrees.sh` | Parallel `/scope` hard isolation from `parallel-scope-lock.json` |
Use `/best-of-n` when comparing models on a flaky gate fix; apply the winner with `/apply-worktree` or commit from the worktree.

## Local compute first

When working on **This Computer**, treat parallel agents + worktrees as the default way to use CPU/RAM:

1. Finish Sequential schema-lock, then **always** run `plan-parallel-dispatch` — do not keep independent scopes on one agent
2. Launch concurrent Task subagents in **one** assistant message (`/scope`)
3. Prefer native `/worktree` isolation for heavy installs/tests so the main tree stays responsive
4. Prefer `/best-of-n` over serial model retries for flaky gates
5. Prefer local Agent + CLI over Cloud Agents unless the machine is unavailable or commercial Autofix/Automations are intentional

Override gate worker count with `BOOTSTRAP_CHECK_JOBS` (see `scripts/lib/run_checks_parallel.py`).

## Sprint 1 (child repo) defaults

| Stack | Isolated scope |
|-------|----------------|
| web | `examples/web/**` |
| python | `examples/python/**` |
| android | `examples/android/**` |
| node | `examples/node/**` |
| multi | One scope per stack row; no overlap |
| none | Match `AGENT_MEMORY.md` checked modules |
## Sprint M13 (template maintainer) — archived

> Completed 2026-06-15. Branch protection verify, init `--stack`, local APK hash wrapper.

| Agent | Scope |
|-------|-------|
| A — Branch protection | `scripts/verify-branch-protection.sh`, `.ps1`, `run-maintainer-gates.sh` |
| B — Init CLI | `scripts/init-project.sh`, `scripts/init-project.ps1` |
| C — APK reproducibility | `scripts/verify-reproducible-apk.sh`, `.ps1`, `modules/android/MODULE.md` |
## Sprint M14 (template maintainer) — archived

> Completed 2026-06-15. Version sync, init.ps1 fix, gate wiring, web/Android hardening. See `COMPLETED_TASKS.md`.

## Sprint M15 (template maintainer) — archived

> Completed 2026-06-15. P2 backlog post-0.9.0. See `COMPLETED_TASKS.md`.

## Sprint M17 (template maintainer) — archived

> Completed 2026-06-15. Post-M16 code review. See `COMPLETED_TASKS.md`.

## Sprint M18 (template maintainer) — archived

> Completed 2026-06-16. Post-P2 code review on `f59023c`. See `COMPLETED_TASKS.md`.

## Sprint M18 P2 backlog (template maintainer) — archived

> Completed 2026-06-16. See `COMPLETED_TASKS.md`.

## Sprint M16 (template maintainer) — archived

> Completed 2026-06-15. Tag-gate timing, SBOM version validation, CI/docs parity, P2 backlog. See `COMPLETED_TASKS.md`.

| Agent | Scope |
|-------|-------|
| A — Release + CI gates | `release.yml`, `check-github-ci.sh`, `ci.yml` |
| B — Init + upgrade sim | `init-project.sh`, `init-project.ps1`, `simulate-template-upgrade.sh` |
| C — Web a11y + e2e | `AboutPanel.ts`, `e2e/app.spec.ts`, `appBootstrap.test.ts` |
| D — Docs hygiene | `OPTIONAL_STACKS.md`, `MAINTAINING_THE_TEMPLATE.md`, `SECURITY_TRIAGE.md` |
## Sprint M12 (template maintainer) — archived

> Completed 2026-06-15. CodeQL init order + Kotlin 2.3.20 pin; tag gate `--jobs`; bootstrap coverage.

| Agent | Scope |
|-------|-------|
| A — CodeQL + release gate | `.github/workflows/codeql.yml`, `release.yml`, `scripts/check-github-ci.ps1` |
| B — Android tests + restart UI | `examples/android/app/src/test/**`, `src/androidTest/**`, `ui/GoldenPathApp.kt` |
| C — Web bootstrap | `examples/web/src/appBootstrap.ts`, `vitest.config.ts` |
| D — Docs + CHANGELOG | `docs/FEATURE_MODULES.md`, `CHANGELOG.md`, `.cursor/rules/feature-modules.mdc` |
## Sprint M11 (template maintainer) — archived

> Completed 2026-06-15. CI blocker fixes on `9163dab` follow-up.

## Sprint M10 (template maintainer) — archived

> Completed 2026-06-15. Reference scopes for future maintainer parallel work.

| Agent | Scope |
|-------|-------|
| A — Web settings | `examples/web/src/components/SettingsPanel.ts`, `examples/web/src/locales/**`, `examples/web/src/style.css`, `examples/web/e2e/**` |
| B — Android settings/About | `examples/android/app/src/main/java/dev/foss/goldenpath/ui/**`, `examples/android/app/src/main/res/**`, `examples/android/app/src/main/assets/**` |
| C — Init + gates | `scripts/init-project.sh`, `scripts/init-project.ps1`, `scripts/init-stack-sync.py`, `scripts/setup-github-repo.sh`, `scripts/check-security-triage.sh`, `scripts/pre-release-gate.sh`, `scripts/run-maintainer-gates.sh` |
| D — Docs | `docs/**`, `modules/**`, `SECURITY.md` |
**Sequential-only (no Parallel):** `BUILD_PLAN.md`, `examples/web/src/AppShell.ts`, `examples/web/src/main.ts`, `MainActivity.kt`, `.github/workflows/release.yml`

## Sprint M9 (template maintainer) — archived

> Completed 2026-06-15. Reference scopes for future maintainer parallel work.

| Agent | Scope |
|-------|-------|
| A — Web | `examples/web/src/**`, `examples/web/e2e/**` |
| B — Android | `examples/android/app/**` |
| C — Gates/CI | `scripts/feature-gate.sh`, `scripts/feature-autofix.sh`, `scripts/run-maintainer-gates.sh`, `scripts/pre-release-gate.sh`, `scripts/check-security-triage.sh`, `.github/workflows/release.yml`, `.github/workflows/ci.yml` |
| D — Docs | `docs/**`, `.cursor/rules/**`, `TEMPLATE_INDEX.json`, `modules/**` |
**Sequential-only (no Parallel):** `BUILD_PLAN.md`, `scripts/agent-progress.sh`, `scripts/watch-agent-gates.sh`, shared `design-tokens/`, shared `branding/`

## Sprint M1 (template maintainer) — archived

| Task | Scope |
|------|-------|
| Web bundle + snapshots | `examples/web/**`, `scripts/check-bundle-size.sh` |
| Python pyright | `examples/python/**` |
| Android metadata | `examples/android/fastlane/**`, `examples/android/README.md` |
| Cursor rules | `.cursor/rules/testing.mdc`, `.cursor/rules/ci-gates.mdc` |
| Scorecard | `.github/workflows/scorecard.yml` |
| Parallel checker | `scripts/check-parallel-scope.sh`, `docs/PARALLEL_AGENT_SCOPES.md` |
## Collision Response

If `check-parallel-scope.sh` fails, split the task or move one item back to Sequential lane.
