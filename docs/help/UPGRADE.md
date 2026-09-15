# Upgrade (any IDE)

Ask your agent to compare this project to the parent template and **plan** updates. In Cursor you can type `/upgrade` instead.

This is not `/maintain` (weekly security). It does not overwrite your app until you pick item numbers.

## Paste prompt

```
Read docs/help/UPGRADE.md and report template gaps for this repo. Do not edit files. Do not implement unless I name numbers.

```

## Recipe

1. If this repo is still the bootstrap template (`bootstrap.config.json` purpose/name), run `python3 scripts/agent-run.py simulate-template-upgrade` and stop.
2. Otherwise this is a **child**. Read **Template gaps (synced)** on `BUILD_PLAN.md` if present, then run `python3 scripts/agent-run.py check-template-updates` and `python3 scripts/agent-run.py check-template-gaps` (or refresh with `sync-template-gaps-build-plan --apply`).
3. Print numbered items in four buckets: Canon (safe copy later), Mixed (merge, keep child values), Sacred (human only — never overwrite), Golden Path (missing slices for the active stack; adopt with `/feature`, do not copy `examples/` over the app).
4. Write a short Plan with `### Critique`. Policy table: `docs/UPGRADING_FROM_TEMPLATE.md`.
5. Offer: “Say the number(s) to add 🔲 BUILD_PLAN rows.” Do not write the board until then (the synced block is cron-managed; named numbers still gate `/upgrade` apply).
6. **No silent `do all`.** If they say `do all` / `implement all` / `add all`, do not add rows. Wait for numbered confirmation.

## Sacred (never blind-overwrite)

`AGENTS.md`, `AGENT.md`, `docs/spec.md`, `docs/plan.md`, `docs/INITIALIZATION_PROMPT.md`, live `.env`, live `scratchpad.md`, `LICENSE`, product `examples/`.

See [`AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md) if your tool has no slash commands.
