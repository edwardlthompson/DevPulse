# Cursor CLI (FOSS opt-in)

Terminal / headless Cursor Agent for local or CI use. **Meaningful runs need a `CURSOR_API_KEY`** (usage billed). This is the FOSS alternative to Cloud Agents for automated gate triage.

## Modes vs repo modes

| CLI / product | Repo router |
|---------------|-------------|
| Plan / Ask / Debug in CLI | Same intent as [`CURSOR_MODES.md`](CURSOR_MODES.md) |
| Built-in `/plan` | Distinct from batch [`.cursor/commands/plan.md`](../.cursor/commands/plan.md) |
| Cloud handoff | Commercial — see [`CURSOR_COMMERCIAL_ACTIVATION.md`](CURSOR_COMMERCIAL_ACTIVATION.md) |
## Local install

Follow [Cursor CLI overview](https://cursor.com/docs/cli/overview.md) and [headless](https://cursor.com/docs/cli/headless.md). Set `CURSOR_API_KEY` in the environment — never commit it.

Prefer restricted autonomy: edit only when needed; deny `git push` (project [`.cursor/permissions.json`](../.cursor/permissions.json) + hooks).

## GitHub Actions (opt-in)

The example workflow is **outside** `.github/workflows/` so CI never auto-runs it:

[`.github/workflow-examples/cursor-agent.yml`](../.github/workflow-examples/cursor-agent.yml)

Enable:

1. Copy to `.github/workflows/cursor-agent.yml`
2. Add repository secret `CURSOR_API_KEY`
3. Pin CLI install per [GitHub Actions docs](https://cursor.com/docs/cli/github-actions.md)
4. Keep `workflow_dispatch` only; do not add `push` triggers for spend control
5. Never allow `git push` from the agent prompt

Default FOSS CI remains the existing workflows (no Cursor API key required).

## FOSS path vs Cloud Agents

| Path | When |
|------|------|
| Local gates + `/fix` + this CLI example | FOSS / no Cloud billing |
| Cloud Agents + Bugbot Autofix + Automations | Commercial tier — see commercial docs |
## Related

- [`CURSOR_INTEGRATIONS.md`](CURSOR_INTEGRATIONS.md)
- [`CURSOR_MODES.md`](CURSOR_MODES.md)
- [`CODEX_REVIEW.md`](CODEX_REVIEW.md) — third-party Codex review (separate `OPENAI_API_KEY`)
