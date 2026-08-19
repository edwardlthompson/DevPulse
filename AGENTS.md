# Agent Router

1. **First read:** `docs/START_HERE.md`
2. **Cursor modes:** `docs/CURSOR_MODES.md` (Ask / Plan / Agent / Debug routing)
3. **Why / coach:** `docs/BEST_PRACTICES.md` · 30-day playbook `docs/FIRST_30_DAYS.md` · `/coach` · backlog `/ideas` (`docs/help/IDEAS.md`) · first-run `/tour` (`docs/help/TOUR.md` in other IDEs) · portability `docs/AGENT_PORTABILITY.md`
4. **Bootstrap mode:** `docs/INITIALIZATION_PROMPT.md`
5. **Reference mode:** `docs/FOR_AGENTS.md` + `TEMPLATE_INDEX.json`
6. **Task board:** `BUILD_PLAN.md` (Sequential before Parallel) — status: 🔲 open · ✅ done · ❌ blocked
7. **Parallel dispatch:** parallel-first BUILD_PLAN; `/build` automates HUMAN/ADB first, backlogs failures to `HUMAN_BACKLOG.md`, never halts on human labels — `scripts/build-sprint-status.sh --lane child`
8. **Living memory:** update `AGENT_MEMORY.md` only at milestone boundaries

> Legacy `.cursorrules` is deprecated. Use `.cursor/rules/*.mdc` and this file instead.

## Project Overview & Architecture

<!-- bootstrap-project-card -->
**Product:** agent-project-bootstrap
**Purpose:** GitHub Template for FOSS coding-agent projects
**Stack:** multi
<!-- /bootstrap-project-card -->

This repository is a **GitHub Template** for FOSS projects with coding agents (Cursor, Windsurf, Antigravity, Claude Code, Copilot, Gemini CLI, Aider, Cline). Child repos start from **Use this template**, then `scripts/init-project.sh` (or `.ps1`). Edit `AGENTS.md`, then `bash scripts/bootstrap-lifecycle.sh --sync-adapters`.

- **Composition:** stack modules (`modules/{stack}/`) + Golden Path examples (`examples/{stack}/`) + agent routing
- **Lifecycle:** preflight → init (stack, branding, prune) → post hooks (adapters, checklist, manifest)
- **Manifest:** `bootstrap.config.json` (schema in `bootstrap.config.json.example`)
- **Product spec:** `docs/spec.md` · plan stub: `docs/plan.md` · feature slices: `docs/features/`

## Environment & Dependency Management

| Tool | Role |
|------|------|
| Python 3.11+ | Init, gates, adapters (`scripts/lib/resolve-python.sh`) |
| Git | Required — preflight fails if missing |
| Node 22 + npm | Web / Node stacks |
| uv | Python stack |
| JDK 17+ | Android stack |
Copy `.env.example` → `.env` (never commit `.env`). Lockfiles are required when a stack is present.

## Build, Test, and Validation Commands

**Before marking a BUILD_PLAN row ✅**, run the verification harness:

```bash
bash scripts/verify.sh
# or
python3 scripts/agent-run.py verify

```

`--full` also runs `feature-gate` for the active stack. Do not mark the task complete if verify fails.

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py feature-gate --stack <active>
python3 scripts/agent-run.py watch-agent-gates --once --autofix
python3 scripts/agent-run.py check-repo-hygiene

```

Stack tests: web `npm test`; python `uv run pytest`; Android `./gradlew test`. After init: `PROJECT_CHECKLIST.md`.

## Architecture Constraints

- Pure FOSS under MIT license; no proprietary closed-source SDKs in production path
- Max 300 lines per static data file (UI + i18n), 150 lines per pure logic file
- Strict type safety and runtime validation at all data boundaries
- Core business logic decoupled from layout framework (MVVM / Clean / Hexagonal)
- Opt-in only telemetry; GDPR/CCPA compliant

## Code Style & Architectural Invariants

- Conventional Commits for all changes
- Small, modular functions; keep files within token-optimal size
- Read-before-write: inspect types/interfaces via `@filename` before editing
- Cursor mode routing per `docs/CURSOR_MODES.md`; Plan for non-trivial tasks with resolved `### Critique` (Issue→Resolution baked into the plan body)

## Testing & Quality Enforcement

**Test-first:** Every `[AGENT]` feature task must add or update automated tests for the change, **or** document in `docs/features/{name}.md` / `docs/spec.md`:

1. Why automated tests are not feasible
2. The fallback validation command (for example `feature-gate.sh` or a named smoke script)

Do not mark a BUILD_PLAN feature row ✅ without tests or that justification. Coverage budgets: `.cursor/rules/testing.mdc`.

## Security Guidelines & Commit Conventions

- Conventional Commits; never commit secrets or `.env`
- Security defaults are on: `SECURITY.md`, Dependabot, CI, CodeQL, secret scanning
- Destructive ops (`git push`, production deploys) need `[HUMAN]` approval (see `.cursor/rules/destructive-ops.mdc`)
- Vulnerability reports: `SECURITY.md` (private reporting)

## Session Protocol

- On session start: read `START_HERE.md`, pick mode via `docs/CURSOR_MODES.md` (roles if your IDE uses other names), then `BUILD_PLAN.md` Sequential lane
- If your tool has no slash commands, use `docs/help/*.md` (start with `docs/help/TOUR.md`)
- When creating or significantly changing a file, state one sentence of why (see `docs/BEST_PRACTICES.md` and `/coach`)
- On milestone end: update `AGENT_MEMORY.md`, append to `DECISION_LOG.md` or `docs/adr/`
- On 3-strike failure: halt and escalate to human
- On context bloat: write `.cursor-session-state`, ask human to clear chat
- Sprint 2+ features: after each AGENT step run `scripts/watch-agent-gates.sh --once --autofix` (see `docs/FEATURE_MODULES.md`)
- Repo hygiene: track source only; run `scripts/check-repo-hygiene.sh` before push (see `docs/REPO_HYGIENE.md`)
- Log significant agent actions in `DECISION_LOG.md` at milestone boundaries

## Multi-Agent Adapters

This file is the source of truth. After editing it, sync adapters:

```bash
bash scripts/bootstrap-lifecycle.sh --sync-adapters

```

| Target | File |
|--------|------|
| Cursor | `.cursor/rules/main.mdc` |
| Claude Code | `CLAUDE.md` |
| GitHub Copilot | `.github/copilot-instructions.md` |
| Gemini / Antigravity | `GEMINI.md` (pointer only — never real rules) |
| Windsurf | `.windsurf/rules/agents-pointer.md` |
| Cline / Roo | `.clinerules` |
| Aider | `CONVENTIONS.md` |
| Continue | `.continue/rules/agents.md` |
Do not hand-edit generated adapters. See `docs/AGENT_PORTABILITY.md`.

## Module Activation

Activate only the modules matching your stack. See `modules/*/MODULE.md`.

## Cursor FOSS integrations

Shipped in template (see `docs/CURSOR_INTEGRATIONS.md`):

- **Hooks** — `.cursor/hooks.json` enforces destructive-ops + UTF-8 (fail-open; `/push` session override)
- **Skills (7)** — `.cursor/skills/` progressive-load companions for `/gates`, `/scope`, `/fix`, hygiene, Sprint 0, features, canvas status
- **Subagents (3)** — `.cursor/agents/` verifier, gate-fixer, explorer
- **Local compute first** — `.cursor/rules/local-compute.mdc`: This Computer + parallel Task/worktrees/`/best-of-n` before Cloud; multi-core bootstrap checks
- **Worktrees** — `.cursor/worktrees.json` + fail-soft OS setup (`/worktree`, `/best-of-n`)
- **Auto-review** — `.cursor/permissions.json` dual layer with hooks
- **Plugin pack** — `.cursor-plugin/plugin.json` + `scripts/pack-cursor-plugin.*` → `dist/cursor-plugin/`
- **CLI (opt-in)** — `docs/CURSOR_CLI.md` + `.github/workflow-examples/cursor-agent.yml`
- **Codex review (opt-in)** — `docs/CODEX_REVIEW.md` + `/codex-review` + `.github/workflow-examples/codex-review.yml` (used by expanded `/prerelease` / `/ship`)
- **Optional MCP** — copy `.cursor/mcp.foss.example` → gitignored `.cursor/mcp.json`

Validate: `python3 scripts/agent-run.py check-cursor-hooks -- --smoke`, `python3 scripts/agent-run.py check-cursor-integrations -- --tier foss`

## Cursor Commercial integrations

Hidden on FOSS bootstrap (`distribution_tier: foss` in `.cursor/stack-selection.json`). When `--distribution-tier commercial`:

- `commercial-compliance.mdc` replaces active FOSS compliance rule
- Activate Cloud/Bugbot/MCP via `docs/CURSOR_COMMERCIAL_ACTIVATION.md`
- Android proprietary patterns: `modules/android/COMMERCIAL.md`

Router reads `distribution_tier` from `.cursor/stack-selection.json` (set by `init-project.sh`).

## Ecosystem-Specific Rules

- **Android:** FOSS only; reproducible builds with `SOURCE_DATE_EPOCH`
- **Web/PWA:** Offline-first service workers; Lighthouse budget gates
- **Python:** Strict typing (mypy), ruff lint/format, locked dependencies
- **Lightroom:** Adobe SDK Lua API only (`Lr*` namespaces)
