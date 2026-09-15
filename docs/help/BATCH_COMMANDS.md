# Agent shortcuts (cheat sheet)

Shortcut commands for Cursor Agent — type `/` in Agent chat to pick a recipe.

**Any other IDE** (Windsurf, Antigravity, Claude Code, Copilot, Aider, Cline): paste `Read docs/help/TOUR.md and walk me through it.` Slash commands are Cursor-only; the same recipes live in this `docs/help/` folder. See [`docs/AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md).

## 30-second start

1. Open **Agent** chat in Cursor.
2. Type **`/`** to open the command menu.
3. Pick a command (e.g. `/tour`, `/bootstrap`, `/verify`, `/build`).
4. The agent runs the workflow step by step.

**Cline path (no slash commands):** Open this project in Cursor. Install recommended extensions if prompted, or search Extensions for Cline (`saoudrizwan.claude-dev`). Click the Cline icon, Sign In with GitHub (Google/email ok). Do not paste API keys, install Codex, or set `OPENAI_API_KEY`. Set API Provider = Cline and pick a FREE model. Paste: `Read docs/help/TOUR.md and walk me through it. Follow AGENTS.md.` Review every diff; run `python3 scripts/agent-run.py verify` before trusting changes. Full steps: [`CLINE.md`](CLINE.md).

Bookmark this page for when you come back after a break. **Print every command:** open [`batch-commands-print.html`](batch-commands-print.html) in a browser and use Print.

## Try these first (super commands)

| Command | When to use |
|---------|-------------|
| `/bootstrap` | Brand-new project — Sprint 0 setup, then a Welcome Tour (`/tour`) |
| `/tour` | 10-minute first-run walk (any IDE: `docs/help/TOUR.md`) |
| `/coach` | What to do next and why (health snapshot + 30-day playbook). Other IDEs: [`COACH.md`](COACH.md) |
| `/ideas` | Ranked backlog of in-scope next features (does not implement) |
| `/allideas` | Complete in-scope dump to fill BUILD_PLAN (does not implement until you say `board`) |
| `/verify` | After your changes, before opening a pull request |
| `/build` | Run BUILD_PLAN end-to-end — per-row gates only dirty stacks; wrap-up smokes every ✅ row then `/gates` |
| `/ship` | Publish a release to GitHub (runs checks, push, post-release) |
| `/maintain` | Weekly health pass — security, dependencies, full review |
**If CI is red:** run `/fix` or `/ci` + `/gates` and get required checks green **before** `/ideas` or `/allideas`. A red Monday cron or failing `main` is not a backlog brainstorming day.

**Ship-first (M58/M59):** If `[Unreleased]` has notes and open AGENT/AUTO is zero, run `/ship` (or `/prerelease`) before `/allideas`. M58 = Espresso/ship CI; M59 = CI harden + stacks.

**Worked example — new project:** clone your repo → open your agent → in Cursor type `/bootstrap` (elsewhere: ask it to follow `docs/help/TOUR.md` after init). The agent walks through init, stack setup, GitHub settings, validation gates, and `/tour`. Type `/coach` later for the next recommended action.

## When you need one step

Grouped by life moment (not every command — use `/` menu for the full list).

**Getting started:** `/tour` · `/init` · `/setup` · `/prune` · `/gates` · `/coach` · `/ideas` · `/allideas`

**Building:** `/plan` · `/adr` (next `docs/adr/` record) · `/feature` · `/fix` (gates failed after `/build`) · `/cleanup` (archive finished BUILD_PLAN rows) · `/scope` (parallel manifest + auto Task dispatch)

**Docs & checks:** `/docs` · `/ci` (CI poll only) · `/gates` (full local validation, including compute probe; always render the canvas status overview). **bootstrap-doctor** is an alias for the same maintainer/validate gates: `python3 scripts/agent-run.py validate-bootstrap --quick` or `python3 scripts/agent-run.py run-maintainer-gates`.

**Publishing:** `/update-deps` (local bumps) · `/prerelease` (autofix + `--local` gate) · `/push` (commit + push + release) · `/regress` (after release)

**Local hardware:** `/best-of-n` (worktree model race) · `/emulator` (optional AOSP GPU tests; skips without SDK)

**Maintenance:** `/triage` · `/update-deps` · `/dependabot` (GitHub leftover) · `/audit` (full repo review) · `/upgrade` (child: template gap plan only; this template: upgrade sim)

`/ship` runs `/update-deps` then `/prerelease` — one command for local bumps, autofix, local gates, push, and regress. Third-party/autonomous work for a new user is Cline in Cursor (human-in-the-loop diffs), not a paid CLI.

**Long sessions:** `/compact` (save checkpoint before clearing chat) · `/restore` (load checkpoint) · `/resume` (after Cloud Agent work on another machine)

**Print this list:** open [`docs/help/batch-commands-print.html`](batch-commands-print.html) in a browser and use Print (Ctrl+P). Novice wording, every command, no PDF required.

## Before you publish

`/push` and `/ship` **push code to GitHub**. Only run them when you intend to publish. `/ship` is the full path (local dep update → pre-release checks → push → post-release verification). Use `/prerelease` alone if you want checks without pushing yet.

After a GitHub Release tag, wait for SBOM + OpenVEX assets (Release workflow can lag):

```bash
python3 scripts/agent-run.py wait-release-sbom -- --wait 300
# or: bash scripts/wait-release-sbom.sh v1.2.1 --wait 300

```

## Coming back after a break?

Same menu: type **`/`** in Agent chat. Supers like `/verify` or `/bootstrap` are a good refresher. Keep this file bookmarked. After Cloud Agents, type **`/resume`** so the PC agent fetches, syncs open Dependabot/release PRs onto BUILD_PLAN, lists leftover `cursor/*` PRs, and names the next AGENT row.

## Bare words (optional)

You can type a single word like `audit` instead of `/audit`. Slash commands are more reliable — use them if a bare word is ignored.

---

Advanced registry (maintainers): [docs/BATCH_COMMANDS.md](../BATCH_COMMANDS.md)

## `/allideas` → board → `/build`

1. Type `/allideas` for a full dump.
2. Reply `board` or name numbers to fill `BUILD_PLAN.md`.
3. Type `/build` to execute (no approval pause). Archive with `/cleanup` after smoke.
