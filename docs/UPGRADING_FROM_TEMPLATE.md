# Upgrading From Template

Child repos do not auto-merge file copies from the upstream template. Monday GitHub cron (`weekly-health-check.yml`) **does** refresh a managed **Template gaps (synced)** block on `BUILD_PLAN.md` from `check-template-gaps` (plan-only). Use this guide when that block (or the update checker) shows you are behind.

In Cursor, type `/upgrade`. On a **child** the agent compares this project to the template and writes a plan. It does not apply changes until you name item numbers. Other IDEs: [`docs/help/UPGRADE.md`](help/UPGRADE.md). On **this** template repo, `/upgrade` still runs the upgrade simulation.

## Step 1: Read the Notification

Run `scripts/check-template-updates.sh` or check the devcontainer postStart output. On a child, also open **Template gaps (synced)** on `BUILD_PLAN.md` (Monday cron via `sync-template-gaps-build-plan`). The agent entry is `/upgrade` (also runs `check-template-gaps`).

## Step 2: Review CHANGELOG

Read the upstream release notes at `github.com/edwardlthompson/agent-project-bootstrap/releases`.

## Step 3: Cherry-Pick by Area

**Re-run policy:** **Canon** — overwrite with upstream. **Mixed** — merge (child may have local lines). **Sacred** — never blind-overwrite.

| Changed area | Strategy | Owner | Re-run policy |
|-------------|----------|-------|---------------|
| `.github/workflows/` | Cherry-pick or manual merge | AGENT + HUMAN review | Mixed |
| `.gitignore` | Merge new ignore rules | AGENT | Mixed |
| `.cursor/rules/` | Copy new/changed `.mdc` files | AGENT | Canon |
| `docs/CURSOR_MODES.md` | Copy; canonical Cursor mode router | AGENT | Canon |
| `.cursor/rules/cursor-modes.mdc` | Copy with other rules | AGENT | Canon |
| `.cursor/commands/` | Copy all slash command files | AGENT | Canon |
| `.cursor/rules/batch-commands.mdc` | Copy with other rules | AGENT | Canon |
| `docs/help/BATCH_COMMANDS.md` | Copy human cheat sheet | AGENT | Canon |
| `docs/BATCH_COMMANDS.md` | Copy agent registry | AGENT | Canon |
| `CODE_REVIEW.md.example` | Copy audit template | AGENT | Canon |
| `RELEASE_NOTES.md.example` | Copy release draft template | AGENT | Canon |
| `scratchpad.md.example` | Copy working-memory stub | AGENT | Canon |
| `docs/features/_handoff.md` | Copy parallel handoff stub | AGENT | Canon |
| `scripts/check-batch-commands.sh` | Copy with validate-bootstrap | AGENT | Canon |
| `docs/INITIALIZATION_PROMPT.md` | Manual review; do not blind overwrite | HUMAN | Sacred |
| Child `AGENTS.md` (after init) | Never blind-overwrite | HUMAN | Sacred |
| Child `AGENT.md` (original brief) | Never blind-overwrite | HUMAN | Sacred |
| `docs/spec.md`, `docs/plan.md` | Merge product text; keep section headings | HUMAN | Sacred |
| `CLAUDE.md`, `GEMINI.md`, `CONVENTIONS.md`, `.clinerules`, `.github/copilot-instructions.md`, `.cursor/rules/main.mdc`, `.windsurf/rules/`, `.continue/rules/` | Re-run `bootstrap-lifecycle.sh --sync-adapters` after AGENTS.md merge | AGENT | Canon |
| `bootstrap.config.json` | Merge keys; keep child values | AGENT | Mixed |
| `PROJECT_CHECKLIST.md` | Keep child progress; add new rows from upstream | HUMAN | Mixed |
| `BUILD_PLAN_TEMPLATE.md` | Copy child board model; do not overwrite live `BUILD_PLAN.md` | AGENT | Canon |
| `BUILD_PLAN.md` | Keep child rows; refresh legend/tally/smoke from the template file | HUMAN | Mixed |
| `scripts/` | Copy updated template scripts | AGENT | Canon |
| `scripts/check-file-encoding.sh` | Copy + add CI/pre-commit gate | AGENT | Canon |
| `scripts/validate-bootstrap.sh` | Copy expanded validation | AGENT | Canon |
| `scripts/check-changelog-unreleased.sh` | Copy with validate-bootstrap | AGENT | Canon |
| `scripts/check-license-compliance.sh` | Copy strict license gate | AGENT | Canon |
| `.github/workflows/dependency-review.yml` | Cherry-pick workflow | AGENT + HUMAN review | Mixed |
| `.cursor/rules/destructive-ops.mdc` | Copy new rule file | AGENT | Canon |
| `.env.example` | Merge new vars; never overwrite local `.env` | AGENT | Mixed |
| Live `.env`, `scratchpad.md`, `CODE_REVIEW.md` | Never overwrite | HUMAN | Sacred |
| `LICENSE` | Verify MIT still applies | HUMAN | Sacred |
| `examples/` | Reference only unless adopting new stack | HUMAN decision | Sacred |
| `TEMPLATE_INDEX.json` | Merge then run validate script | AGENT | Mixed |
## Version Compatibility

| Upgrade | Notes |
|---------|-------|
| 0.1.x → 0.1.y | Safe PATCH; cherry-pick freely |
| 0.1.x → 0.2.0 | Check CHANGELOG for new files/schema changes |
| 0.x → 1.0.0 | Full review; init prompt structure may have changed |
## Decision Points

- `[HUMAN]` Approve which upstream changes to adopt
- `[AGENT]` Apply diffs to matching files
- `[AUTO]` CI validates after merge
