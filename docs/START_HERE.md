# Start Here

> **Read this file first** — whether you are a human or any coding agent.

## What is this?

`agent-project-bootstrap` is a **GitHub Template Repository** for bootstrapping FOSS projects with coding agents (Cursor, Windsurf, Antigravity, Claude Code, Copilot, and others). Shared contract: [`AGENTS.md`](../AGENTS.md). Tool map: [`AGENT_PORTABILITY.md`](AGENT_PORTABILITY.md). Word list: [`help/GLOSSARY.md`](help/GLOSSARY.md) — [**Sacred**](help/GLOSSARY.md), [**Canon**](help/GLOSSARY.md), [**AGENT**](help/GLOSSARY.md) / [**HUMAN**](help/GLOSSARY.md) / [**ADB**](help/GLOSSARY.md) / [**AUTO**](help/GLOSSARY.md).

## Which repo mode are you in?

- [**Bootstrap**](help/GLOSSARY.md): New project from **Use this template** → write `AGENT.md` from `AGENT.md.example` (original brief, before init), then read `docs/CURSOR_MODES.md` and `docs/INITIALIZATION_PROMPT.md`
- [**Reference**](help/GLOSSARY.md): Existing project using this repo as rules reference → read `docs/CURSOR_MODES.md`, then `docs/FOR_AGENTS.md`

## Cursor modes (Plan / Agent / Debug / Ask)

See [`docs/CURSOR_MODES.md`](CURSOR_MODES.md) — pick the Cursor mode before editing code. On session start, say whether `CHANGELOG.md` `[Unreleased]` has entries, and name the next 🔲 `[AGENT]` BUILD_PLAN row (or that the AGENT board is empty).

## Agent shortcuts (Bootstrap)

In Cursor, type **`/`** in Agent chat. Start with **[docs/help/BATCH_COMMANDS.md](help/BATCH_COMMANDS.md)** — try `/tour` (10 minutes; Settings chrome map: [`help/SETTINGS_ASCII_TOUR.md`](help/SETTINGS_ASCII_TOUR.md)) or `/bootstrap` on a new project, `/verify` before merge. Print every command: [`help/batch-commands-print.html`](help/batch-commands-print.html). On a product repo, `/upgrade` plans template catch-up without overwriting the app.

**First-time path: Cline (free).** Open this project in Cursor. Install recommended extensions if prompted, or search Extensions for Cline (`saoudrizwan.claude-dev`). Click the Cline icon, Sign In with GitHub (Google/email ok). Do not paste API keys, install Codex, or set `OPENAI_API_KEY`. Set API Provider = Cline and pick a FREE model. Paste: `Read docs/help/TOUR.md and walk me through it. Follow AGENTS.md.` Review every diff; run `python3 scripts/agent-run.py verify` before trusting changes. Full steps: [`help/CLINE.md`](help/CLINE.md).

In Windsurf, Antigravity, or any other agent: ask it to read [`docs/help/TOUR.md`](help/TOUR.md) (first run) or [`docs/help/COACH.md`](help/COACH.md) (what next).

Optional commercial Grok Bots (not required to build or ship): [`GROK_BOTS.md`](GROK_BOTS.md). OpenSSF Best Practices (project 14564): [`OPENSSF_BEST_PRACTICES.md`](OPENSSF_BEST_PRACTICES.md).

## Bootstrap Read Order

1. `README.md`
2. `docs/START_HERE.md`
3. `docs/CURSOR_MODES.md`
4. `docs/BEST_PRACTICES.md` (why each convention exists) + `docs/FIRST_30_DAYS.md` (+ `docs/LINUX_DEV.md` on Linux)
5. `docs/INITIALIZATION_PROMPT.md`
6. `AGENTS.md` (thin adapters via `--sync-adapters`; see `docs/AGENT_PORTABILITY.md`)
7. `docs/spec.md` + `docs/plan.md` (product spec and milestone stub)
8. `BUILD_PLAN.md` Sequential lane
9. Active `modules/{stack}/MODULE.md` only
10. Active `examples/{stack}/` only
11. `docs/WEB_PROJECT_LAYOUT.md` when stack includes web (folder roles, GitHub Pages)
12. `docs/DESIGN_GUIDE.md` when stack includes web or Android UI (tokens, themes, i18n)
13. `branding/BRANDING.md` for logos, official colors, and pitch README generation
14. `docs/FEATURE_MODULES.md` when implementing Sprint 2+ incremental features (vertical slices)

```mermaid
flowchart TD
  Readme[README] --> Start[START_HERE]
  Start --> Modes[CURSOR_MODES]
  Modes --> Why[BEST_PRACTICES]
  Why --> Init[INITIALIZATION_PROMPT]
  Init --> Agents[AGENTS.md]
  Agents --> Board[BUILD_PLAN]

```

## Reference Read Order

1. `docs/START_HERE.md`
2. `docs/CURSOR_MODES.md`
3. `docs/FOR_AGENTS.md`
4. `TEMPLATE_INDEX.json`
5. `AGENTS.md`
6. Matching `modules/{stack}/MODULE.md` only

## CRITICAL NOTES (phase transitions)

- After **Sprint 0** sign-off: stop treating `docs/INITIALIZATION_PROMPT.md` as the daily read. Follow BUILD_PLAN Sequential, then `/feature` for Sprint 2+ (`docs/features/{name}.md` from `_template.md`, locked API, then Parallel slices).
- Working notes go in gitignored `scratchpad.md` (copy `scratchpad.md.example`). **Reset** on sprint/phase change. Persistent memory stays in `AGENT_MEMORY.md`.
- Child board model: [`BUILD_PLAN_TEMPLATE.md`](../BUILD_PLAN_TEMPLATE.md) — becomes `BUILD_PLAN.md` after init. This repo’s live board stays [`BUILD_PLAN.md`](../BUILD_PLAN.md).

## Do Not Read Yet

- Inactive `examples/` folders
- `KNOWLEDGE_BASE.md` — reference when debugging (KB-001–KB-014)
- `docs/MAINTAINING_THE_TEMPLATE.md` (maintainers only)

## BUILD_PLAN Labels

`AGENT` | `HUMAN` | `ADB` | `AUTO` — filter with `grep '\[AGENT\]' BUILD_PLAN.md`. Definitions: [`help/GLOSSARY.md`](help/GLOSSARY.md).

**Status markers:** 🔲 open · ✅ done · ❌ blocked — emoji only (not `- [ ]` checkboxes). Applies to all repo checklists; see legend in `BUILD_PLAN.md`.

## Security

Enable Dependabot alerts on GitHub (Settings → Code security and analysis). Weekly CVE triage: `docs/SECURITY_TRIAGE.md`. Vulnerability reporting: `SECURITY.md`.

## Agent Prompts

[**Bootstrap**](help/GLOSSARY.md): Read @docs/START_HERE.md, @docs/CURSOR_MODES.md, and @docs/INITIALIZATION_PROMPT.md. Pick Cursor mode per CURSOR_MODES. Follow Section 8. Use BUILD_PLAN Sequential lane.

[**Reference**](help/GLOSSARY.md): Read @docs/CURSOR_MODES.md, @docs/FOR_AGENTS.md, and @TEMPLATE_INDEX.json. Pick Cursor mode per CURSOR_MODES. Apply matching rules. Do not copy examples/ wholesale.

## Linux: first `adb` in ~10 minutes

On Linux hosts with an Android SDK (see [`LINUX_DEV.md`](LINUX_DEV.md)):

1. `export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"` and put `platform-tools` on `PATH`.
2. Plug in a phone (USB debugging on) **or** run `python3 scripts/agent-run.py emulator` (KVM).
3. Accept the RSA prompt on-device once; `adb devices` should show `device`.
4. From `examples/android`: `./gradlew installDebug` then open Settings → About.
5. Missing licenses → **human** runs `sdkmanager --licenses` once (agents/scripts never auto-accept; CI allowlists only).

Do not enable Pages analytics or default crash-proxy telemetry on first run.
