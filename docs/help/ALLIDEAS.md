# All ideas (any IDE)

Ask your agent for a **complete** in-scope backlog dump. In Cursor you can type `/allideas` or `/AllIdeas` instead.

This is not “what do I do right now” (`/coach` / `docs/BEST_PRACTICES.md`) and not the short ranked menu (`/ideas` / [`IDEAS.md`](IDEAS.md)). It is “list everything we could add so we can fill BUILD_PLAN and make sizeable `/build` progress.” Do not implement unless you pick items.

## Paste prompt

```
Read docs/help/ALLIDEAS.md and dump every in-scope next idea, grouped by theme. Do not implement. Do not edit BUILD_PLAN unless I say board, add all, or name numbers.

```

## Recipe

1. Read `AGENT_MEMORY.md` (Persistent Context + latest retrospective only), `BUILD_PLAN.md` Sequential, `CHANGELOG.md` `[Unreleased]`, latest `DECISION_LOG.md` entries, `docs/FIRST_30_DAYS.md`, and `docs/help/BATCH_COMMANDS.md`.
2. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`) and `python3 scripts/agent-run.py feedback-inbox`. If the fix inbox is non-empty, say so first and recommend `/audit`. Merge GitHub `enhancement` issues / Ideas discussions into the dump (P1 default; skip items already on BUILD_PLAN).
3. **Template mode** if this repo is still the bootstrap template. **Child mode** otherwise — read `docs/spec.md` and the active Golden Path README.
4. Print every idea not already shipped or already 🔲/`✅` on the board. Group by theme. Each: title, Why, Effort (S/M), Priority (P0/P1/P2). No cap; if more than ~80, pause and offer `continue`.
5. Name the single best next idea.
6. Offer: “Say numbers, `board`, or `add all` to add 🔲 `[AGENT]` rows.” Do not write the board until then.

## Out of scope

Proprietary SDKs on the FOSS path, a second generator CLI, a second memory tree, `.agents/agents.md` as project law, Cloud-only defaults, Pages telemetry, a live crash-proxy without DPIA.

See [`AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md) if your tool has no slash commands.
