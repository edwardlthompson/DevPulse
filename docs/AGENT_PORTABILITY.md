# Agent portability

One project law: **[`AGENTS.md`](../AGENTS.md)**. Every supported tool either reads that file natively or gets a thin generated pointer. Do not copy the spec into per-tool rulebooks.

## First-time start

| Your tool | What it reads | What you type |
|-----------|---------------|---------------|
| Cursor | `AGENTS.md` + `.cursor/rules/main.mdc` | `/tour`, `/bootstrap`, or `/ideas` |
| Windsurf | `AGENTS.md` + `.windsurf/rules/agents-pointer.md` | “Read `docs/help/TOUR.md` and walk me through it.” |
| Antigravity / Gemini CLI | `AGENTS.md` + pointer-only `GEMINI.md` | Same `docs/help/TOUR.md` prompt |
| Claude Code | `AGENTS.md` + `CLAUDE.md` | Same, or Claude’s `/` menu if you add a skill |
| GitHub Copilot | `AGENTS.md` + `.github/copilot-instructions.md` | Same tour prompt in Copilot Chat |
| Aider | `AGENTS.md` + `CONVENTIONS.md` | `/read docs/help/TOUR.md` then ask for the walk |
| Cline / Roo | `AGENTS.md` + `.clinerules` | Same tour prompt |
| Continue | `AGENTS.md` + `.continue/rules/agents.md` | Same tour prompt |
| Codex / other AGENTS.md readers | `AGENTS.md` | Same tour prompt |
Slash commands under `.cursor/commands/` are Cursor-native. The same recipes live in [`docs/help/`](help/) so any agent can follow them. Backlog: [`docs/help/IDEAS.md`](help/IDEAS.md).

## Edit once, re-sync

```bash
# 1. Change project rules only in AGENTS.md
# 2. Refresh pointers
bash scripts/bootstrap-lifecycle.sh --sync-adapters

```

Never hand-edit generated adapters. Never put real rules in `GEMINI.md` — Antigravity treats it as **higher priority** than `AGENTS.md`, so a long GEMINI file would silently override the Sacred spec. The adapter drift gate fails if that file grows past a pointer.

There is no `.agents/agents.md` in this template. Antigravity’s codelab uses that path for a team roster, not project law.

## Modes in other IDEs

[`docs/CURSOR_MODES.md`](CURSOR_MODES.md) names Cursor’s Ask / Plan / Agent / Debug. Treat those as **roles** (explore / design / implement / diagnose) if your IDE uses different labels.

## Disable a pointer

In `bootstrap.config.json` → `agent_adapters`, set a key to `false` (`gemini`, `windsurf`, `cline`, `aider`, `continue`, or the Cursor / Claude / Copilot flags), then re-sync.
