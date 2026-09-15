# Cursor Integrations

Single source mapping Cursor features → repo artifacts → FOSS vs Commercial exposure.

## FOSS quick start (default)

After `scripts/init-project.sh --distribution-tier foss`:

| Layer | Artifact | Status |
|-------|----------|--------|
| Rules | `.cursor/rules/*.mdc` | Shipped (18). `alwaysApply: true` is allowlisted in `scripts/lib/cursor_rule_audit.py`; glob-scoped rules must set `alwaysApply: false`. |
| Commands | `.cursor/commands/*.md` | Shipped (33) |
| Hooks | `.cursor/hooks.json` + `.cursor/hooks/` | Shipped |
| Skills | `.cursor/skills/` (15) | Shipped |
| Subagents | `.cursor/agents/` (3) | Shipped |
| Modes | `docs/CURSOR_MODES.md` | Shipped |
| Worktrees | `.cursor/worktrees.json` + OS setup scripts | Shipped |
| Auto-review | `.cursor/permissions.json` | Shipped |
| Sandbox | `.cursor/sandbox.json.example` → gitignored `sandbox.json` | Example |
| Parallel `/scope` | `scripts/plan-parallel-dispatch.sh` | Shipped |
| Autonomous `/build` | `scripts/build-sprint-status.sh` | Shipped |
| Agent script runner | `scripts/agent-run.py` | Shipped |
| Plugin pack | `.cursor-plugin/plugin.json` + `scripts/pack-cursor-plugin.*` | Example |
| CLI (opt-in) | `.github/workflow-examples/cursor-agent.yml` + `docs/CURSOR_CLI.md` | Example |
| Codex review (advanced/optional, not first-time, not `/ship`) | `.github/workflow-examples/codex-review.yml` + `docs/CODEX_REVIEW.md` + `/codex-review` | Example |
| GitHub + depsonar MCP (optional) | Copy `.cursor/mcp.foss.example` → `.cursor/mcp.json` | Example |
Validation: `python3 scripts/agent-run.py check-cursor-integrations -- --tier foss`

## Commercial quick start

After `scripts/init-project.sh --distribution-tier commercial`:

- All FOSS layers above remain enabled
- `sync-cursor-features.py` activates `commercial-compliance.mdc` instead of `foss-compliance.mdc`
- Copy commercial examples per [`CURSOR_COMMERCIAL_ACTIVATION.md`](CURSOR_COMMERCIAL_ACTIVATION.md)
- Optional always-on teammates: [`GROK_BOTS.md`](GROK_BOTS.md) (Android/R8 scouting; no FOSS requirement)

## Hooks

Enforcement complement to rules (M27 — no `beforeSubmitPrompt`):

| Event | Script | Behavior |
|-------|--------|----------|
| `sessionStart` | `session_start_context.py` | Stack/tier one-liner |
| `beforeShellExecution` | `before_shell_guard.py` | Denylist + session `destructive_ops_approved` |
| `afterFileEdit` | `after_edit_encoding.py` | UTF-8 check, fail-open (opt-in fail-closed: `CURSOR_ENCODING_STRICT=1` or `.cursor/encoding-strict`) |
| `subagentStart` | `subagent_scope_inject.py` | Parallel lock scope |
| `beforeMCPExecution` | `mcp_audit.py` | Audit log + FOSS server allowlist (fail-open on parse errors) |
Hooks are Python modules (not `.sh`) so Cursor Agent shell execution does not open hook scripts in the editor.

**Quiet agent shell:** Agents should invoke gates via `python3 scripts/agent-run.py <name> [args]` instead of `bash scripts/<name>.sh`. Workspace `.vscode/settings.json` disables editor auto-reveal when files open in the background.

**Troubleshooting focus steal:** Pin the tab you are editing; optional hook opt-out below. If `.sh` tabs still appear, confirm agents use `agent-run.py` (see `.cursor/commands/`).

**Opt-out:** `<!-- cursor-hooks: off -->` in `BUILD_PLAN.md`

**Session override:** `/push` and `/ship` set `destructive_ops_approved: ["git push"]` via `/compact`.

### Honesty labels (enforced vs instructed)

Hooks **fail-open** (KB-012). Label each control so agents do not over-claim enforcement. Canonical table: [`.cursor/rules/destructive-ops.mdc`](../.cursor/rules/destructive-ops.mdc).

| Control | Label | Notes |
|---------|-------|-------|
| `git push` | Enforced (best-effort) | Denylist unless `/push` or `/ship` session override |
| `git push --force` | Enforced (best-effort) | `--force`/`-f` denied even when `git push` is session-approved |
| `terraform apply`, `DROP TABLE`, `DELETE FROM`, `rm -rf /`, `rm -rf ~`, skip-hooks flags | Enforced (best-effort) | `shell-denylist.txt` + `before_shell_guard.py` |
| Production deploys, disabling CI gates, committing secrets | Instructed | Auto-review steers; Gitleaks is pre-commit when installed |
| UTF-8 `afterFileEdit` | Instructed + best-effort | Fail-open on parse/tool errors. Opt-in fail-closed: `CURSOR_ENCODING_STRICT=1` or `.cursor/encoding-strict` |
| `<!-- cursor-hooks: off -->` | Instructed | Disables shell guards for the session |
Validate: `python3 scripts/agent-run.py check-cursor-hooks -- --smoke`

## Local compute first

On **This Computer**, maximize local parallelism before Cloud:

- Rule: [`.cursor/rules/local-compute.mdc`](../.cursor/rules/local-compute.mdc)
- Parallel `/scope` Task subagents + worktrees + `/best-of-n`
- `validate-bootstrap` runs independent checks via `scripts/lib/run_checks_parallel.py` (RAM-capped workers, override with `BOOTSTRAP_CHECK_JOBS`)
- Multi-stack `feature-gate` runs independent stacks in parallel (`FEATURE_GATE_JOBS`; CI cap 2). `/build` passes `--scope auto` so per-row gates only dirty stacks; `/gates` stays full.
- `/best-of-n` and `/emulator` are slash commands; Ollama recipe: [`docs/LOCAL_MODELS.md`](LOCAL_MODELS.md); Linux DX: [`docs/LINUX_DEV.md`](LINUX_DEV.md)
- Session start hook reminds agents of `local-first cpus=N ram= jobs= ollama=`

## Worktrees

Native Cursor worktrees (Agents Window, `/worktree`, `/best-of-n`, CLI) use:

| File | Role |
|------|------|
| `.cursor/worktrees.json` | Points at OS setup scripts |
| `.cursor/setup-worktree-unix.sh` | Fail-soft Unix/macOS setup |
| `.cursor/setup-worktree-windows.ps1` | Fail-soft Windows setup |
Setup copies only `*.env.example` (never `.env`). Missing stack or package managers → `SKIP` and exit **0**. Corrupt `stack-selection.json` → exit **1**.

Parallel-lock isolation remains `scripts/setup-agent-worktrees.sh` (see [`PARALLEL_AGENT_SCOPES.md`](PARALLEL_AGENT_SCOPES.md)).

## Auto-review and sandbox (dual layer with hooks)

Cursor **Auto-review** (Run Modes) reads [`.cursor/permissions.json`](../.cursor/permissions.json):

- `block_instructions` — push, force-push, production deploys, destructive DB, disabling gates/hooks
- `allow_instructions` — routine `python3 scripts/agent-run.py` local gates

**Dual layer is intentional:** project hooks (`beforeShellExecution`) are the hard FOSS denylist; Auto-review steers the classifier. Double-prompt on `git push` is expected. `/push` and `/ship` clear only the **hook** session flag — Auto-review may still ask.

**Cloud Agents ignore Run Modes** — encode cloud policy in project hooks + commercial environment examples.

Optional sandbox: copy `.cursor/sandbox.json.example` → `.cursor/sandbox.json` (gitignored). See [Run Modes](https://cursor.com/docs/agent/security/run-modes).

## Skills (progressive load)

Commands remain canonical UX. Skills wrap high-churn flows:

| Skill | Command / use |
|-------|----------------|
| `validate-bootstrap` | `/gates` |
| `parallel-scope` | `/scope` |
| `watch-gates-autofix` | `/fix` |
| `check-repo-hygiene` | `/gates`, `/audit` |
| `sprint0-signoff` | Sprint 0 on `BUILD_PLAN_TEMPLATE.md` |
| `feature-vertical-slice` | `/feature` |
| `canvas-bootstrap-status` | `/gates` (Canvas; markdown fallback) |
| `update-deps` | `/update-deps`, `/ship` |
| `best-of-n` | `/best-of-n` |
| `local-models` | `docs/LOCAL_MODELS.md` |
| `linux-dev` | `docs/LINUX_DEV.md` |
| `emulator` | `/emulator` |
| `adr` | `/adr` |
| `blender-icons` | `/feature` icon-factory / blender stack |
## Subagents

| Agent | Role |
|-------|------|
| `verifier` | Readonly post-row gate check |
| `gate-fixer` | Scoped autofix for Parallel dispatch |
| `explorer` | Readonly codebase search (Plan Mode) |
## MCP activation (FOSS)

1. Copy `.cursor/mcp.foss.example` → `.cursor/mcp.json` (gitignored)
2. Set `GITHUB_TOKEN` in environment (GitHub MCP only; depsonar does not need it)
3. Restart Cursor
4. Never commit tokens or live `mcp.json`

## Plugin pack (FOSS local)

Do **not** symlink the repo root into `~/.cursor/plugins/local` (double-loads rules). Pack first:

```bash
python3 scripts/agent-run.py pack-cursor-plugin
# or: bash scripts/pack-cursor-plugin.sh / pwsh scripts/pack-cursor-plugin.ps1

```

Then symlink **`dist/cursor-plugin`** → `~/.cursor/plugins/local/agent-project-bootstrap` and Reload Window. Manifest: [`.cursor-plugin/plugin.json`](../.cursor-plugin/plugin.json). No marketplace publish in-template.

## Optional marketplace (not default)

Runbook: [`CURSOR_MARKETPLACE.md`](CURSOR_MARKETPLACE.md).

Child repos **may** add [wshobson/agents](https://github.com/wshobson/agents) as a Cursor marketplace. Do **not** install it by default — 200+ agents would drown context and fight “one feature per agent” plus local-compute-first.

FOSS default stays the local `.cursor/` pack above. If a child repo opts in, pick plugins that **complement** (not replace) shipped commands:

| Marketplace plugin | Complements | Do not replace |
|--------------------|-------------|----------------|
| `debugging-toolkit` | `/debug` | Debug Mode router in `docs/CURSOR_MODES.md` |
| `git-pr-workflows` | `/push` | Destructive-ops + hook denylist |
| Language plugins | Active stack only | Golden Path examples / MODULE.md |
Never vendor the catalog into `.cursor/`.

## CLI (opt-in)

See [`CURSOR_CLI.md`](CURSOR_CLI.md). Example workflow lives under `.github/workflow-examples/` so default CI never runs it.

## Feature radar (maintainer)

- Registry: [`CURSOR_FEATURE_REGISTRY.json`](CURSOR_FEATURE_REGISTRY.json)
- Rubric: [`CURSOR_FEATURE_RADAR.md`](CURSOR_FEATURE_RADAR.md)
- Script: `python3 scripts/agent-run.py cursor-feature-radar`
- Outputs: gitignored `CURSOR_RADAR_REPORT.md`, `CURSOR_RADAR_BACKLOG.md`, `CURSOR_RADAR_BUILD_PLAN_DRAFT.md`

## Switch tier later

```bash
python3 scripts/sync-cursor-features.py --tier foss|commercial

```

Requires `[HUMAN]` approval when swapping compliance rules.

## Cross-links

- [`docs/CURSOR_MODES.md`](CURSOR_MODES.md)
- [`docs/BATCH_COMMANDS.md`](BATCH_COMMANDS.md)
- [`docs/help/CURSOR_FEATURES.md`](help/CURSOR_FEATURES.md)
