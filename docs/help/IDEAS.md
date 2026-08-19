# Ideas (any IDE)

Ask your agent for an in-scope backlog. In Cursor you can type `/ideas` instead.

This is not “what do I do right now” (`/coach` / `docs/BEST_PRACTICES.md`). It is “what could we add next?” Do not implement unless you pick an item.

## Paste prompt

```
Read docs/help/IDEAS.md and propose 5–8 in-scope next ideas. Do not implement. Do not edit BUILD_PLAN unless I name a number.
```

## Recipe

1. Read `AGENT_MEMORY.md` (Persistent Context + latest retrospective only), `BUILD_PLAN.md` Sequential, `CHANGELOG.md` `[Unreleased]`, latest `DECISION_LOG.md` entries, `docs/FIRST_30_DAYS.md`, and `docs/help/BATCH_COMMANDS.md`.
2. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`).
3. **Template mode** if this repo is still the bootstrap template. **Child mode** otherwise — read `docs/spec.md` and the active Golden Path README.
4. Print 5–8 ideas not already shipped or already 🔲 on the board. Each: title, Why, Effort (S/M), Priority (P0/P1/P2).
5. Name the single best next idea. Cap at 8.
6. Offer: “Say the number to add a 🔲 `[AGENT]` row.” Do not write the board until then.

## Out of scope

Proprietary SDKs on the FOSS path, a second generator CLI, a second memory tree, `.agents/agents.md` as project law, Cloud-only defaults, Pages telemetry.

See [`AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md) if your tool has no slash commands.
