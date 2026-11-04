# Cline first-run (Cursor or VS Code)

Cline is the first-time agent for this project. It is **free**. Display name **Cline**. Marketplace id `saoudrizwan.claude-dev`.

Do **not** paste API keys. Do **not** install Codex. Do **not** set `OPENAI_API_KEY`.

## Steps (free cloud path)

1. Open this project in Cursor or VS Code.
2. Install recommended extensions if prompted, or search Extensions for Cline.
3. Click the Cline icon in the sidebar.
4. Sign In with GitHub (Google/email also ok). Do NOT paste API keys. Do NOT install Codex. Do NOT set OPENAI_API_KEY.
5. In Cline settings, API Provider = **Cline**. Pick a model tagged **FREE** / `(free)` in the picker.
6. Paste: `Read docs/help/TOUR.md and walk me through it. Follow AGENTS.md.`
   Or type `/tour` if project workflows are enabled (see below).
7. Review every diff. Run `python3 scripts/agent-run.py verify` before trusting changes.

## Free models (verify on first run)

Cline’s free tier rotates. As of mid‑2026 verification for this template:

| Prefer | Notes |
|--------|--------|
| **Arcee Trinity Large** (or “Trinity Large Preview”) | Free on Cline provider; Apache 2.0 |
| Any picker row tagged `(free)` / FREE | Prefer these over BYO API keys |

Avoid assuming older promo free models (for example past Grok Code Fast / Devstral promos) still appear — if a name is missing, pick another FREE-tagged model. Optional fully local path: Ollama via Cline’s Ollama provider (`docs/LOCAL_MODELS.md`); not required for first-run.

**Verify:** After Sign In, open the model picker once. Confirm at least one FREE-tagged model is selectable before pasting the tour prompt. If none appear, update Cline from the marketplace and retry — do not paste API keys.

## Slash commands in VS Code / Cline

Template shortcuts are Cline **workflows** under [`.clinerules/workflows/`](../../.clinerules/workflows/). Type `/` in Cline chat (or open Rules & Workflows / scale icon and enable them once after clone).

| Cursor | Cline | Portable paste |
|--------|-------|----------------|
| `/tour` | `/tour` | `Read docs/help/TOUR.md and walk me through it. Follow AGENTS.md.` |
| `/coach` | `/coach` | `Read docs/help/COACH.md and give me one next action.` |
| `/build` | `/build` | Next open `[AGENT]` row in `BUILD_PLAN.md` following `AGENTS.md`. |
| `/verify` | `/verify` | `Run python3 scripts/agent-run.py verify and explain the first failure only.` |

Full table and recovery: [`VSCODE_COMMANDS.md`](VSCODE_COMMANDS.md). Cursor `.cursor/commands/` are unchanged.

## Optional fully local path (Ollama)

No cloud keys. Probe hardware and create a high-context custom model:

```bash
python3 scripts/agent-run.py recommend-model
python3 scripts/agent-run.py setup-local

```

Then set Cline Provider = Ollama, model = the custom name, Context Window = Modelfile `num_ctx`. Details: [`LOCAL_AGENT.md`](LOCAL_AGENT.md) · [`docs/LOCAL_MODELS.md`](../LOCAL_MODELS.md). Troubleshooting: [`LOCAL_TROUBLESHOOTING.md`](LOCAL_TROUBLESHOOTING.md). In Cline you can type `/setup-local`.

## Rules file

`.clinerules/AGENTS.md` points at root [`AGENTS.md`](../../AGENTS.md) and includes a short local/XML hard block. Never put long project rules only in the adapter. Edit root `AGENTS.md`, then:

```bash
bash scripts/bootstrap-lifecycle.sh --sync-adapters

```

Workflows under `.clinerules/workflows/` are static; sync does not overwrite them.

## How Cline works

Plan mode then Act mode. Use checkpoints. Approve file and command steps before they run.

Cline works beside Cursor Agent/Composer. Cline is the autonomous hands that replace Codex for a new user. Codex is optional later, not this first-run path.

Next: [`TOUR.md`](TOUR.md) · session checklist: [`SESSION_START.md`](SESSION_START.md).
