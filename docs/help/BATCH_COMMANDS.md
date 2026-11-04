# Agent shortcuts (cheat sheet)

Type `/` in Cursor Agent chat. Other IDEs: paste the matching `docs/help/` file. Print: [`batch-commands-print.html`](batch-commands-print.html).

**Any other IDE** (Windsurf, Antigravity, Claude Code, Copilot, Aider): paste `Read docs/help/TOUR.md and walk me through it.` Cursor slash commands live under `.cursor/commands/`. **Cline (VS Code):** same short names as workflows under `.clinerules/workflows/` — type `/` in Cline chat (see [`VSCODE_COMMANDS.md`](VSCODE_COMMANDS.md)). Portable recipes also live in this `docs/help/` folder. See [`docs/AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md).

## Supers

| Command | When |
|---------|------|
| `/bootstrap` | New project Sprint 0 |
| `/tour` | First-run walk |
| `/coach` | One next action |
| `/verify` | Before merge |
| `/build` | Run BUILD_PLAN, then archive with `/cleanup` and show slim board |
| `/ship` | Release to GitHub |
| `/maintain` | Weekly health |
Red CI → `/fix` or `/ci` + `/gates` first. Dirty Unreleased + empty AGENT → `/ship`. Empty board → `/allideas`.

## By moment

- **Start:** `/tour` · `/init` · `/setup` · `/setup-local` · `/gates` · `/coach` · `/brand`
- **Build:** `/shape` · `/plan` · `/adr` · `/feature` · `/fix` · `/cleanup` · `/scope`
- **UX:** `/ux-review` · `/ux-apply` · aliases `/ui-review` `/ux-audit` `/ui-audit`
- **Publish:** `/update-deps` · `/prerelease` · `/push` · `/regress`
- **Local:** `/best-of-n` · `/emulator` · `/recommend-model` · `/compress-memory` (opt-in Ollama: [`LOCAL_AGENT.md`](LOCAL_AGENT.md))
- **Maintain:** `/triage` · `/dependabot` · `/audit` · `/upgrade`
- **Handoff:** `/compact` · `/restore` · `/resume`
- **Agent mode:** `/lean` · `/balanced` · `/speed` · `/mode` · `/status` ([ADR-0010](../adr/0010-agent-mode-speed-balanced-lean.md))

**Cline path:** Open this project in Cursor or VS Code. Install Cline (`saoudrizwan.claude-dev`). Sign In with GitHub. Prefer FREE model or optional local Ollama ([`LOCAL_AGENT.md`](LOCAL_AGENT.md)). Type `/tour` / `/coach` / `/build` / `/setup-local` after enabling project workflows, or paste: `Read docs/help/TOUR.md and walk me through it. Follow AGENTS.md.` Full steps: [`CLINE.md`](CLINE.md) · cheat sheet: [`VSCODE_COMMANDS.md`](VSCODE_COMMANDS.md).

`/gates` = agent-fast + dirty stacks; `/gates --full` = multi. Registry: [`docs/BATCH_COMMANDS.md`](../BATCH_COMMANDS.md). Cline: [`CLINE.md`](CLINE.md).
