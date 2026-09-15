# Coach (any IDE)

Ask your agent what to do next and why. In Cursor you can type `/coach` instead.

This is not a backlog (`/ideas` / [`IDEAS.md`](IDEAS.md) or `/allideas` / [`ALLIDEAS.md`](ALLIDEAS.md)). It is the next action now.

## Paste prompt

```
Read docs/help/COACH.md and tell me the next recommended action and why. Do not implement unless I ask.

```

## Recipe

1. Read `AGENT_MEMORY.md` (Persistent Context + latest retrospective only), `BUILD_PLAN.md` Sequential, `docs/BEST_PRACTICES.md`, and `docs/FIRST_30_DAYS.md`.
2. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`) and `python3 scripts/agent-run.py feedback-inbox`. Summarize stack, repo mode (template vs child), next BUILD_PLAN row, and any dirty Unreleased / unpushed note. If the fix inbox is non-empty, next action is `/audit`. If `ollama=up`, mention `docs/LOCAL_MODELS.md`. If this is a child repo, run `python3 scripts/agent-run.py check-template-updates`; if it prints a newer template version, offer `/upgrade` (gap plan only).
3. Compare dirty Unreleased vs empty board (`open_agent_auto` vs `[Unreleased]` list items): open AGENT/AUTO → `/build` and name `next_row` (active milestone, e.g. **M59**); no AGENT rows + dirty Unreleased → `/ship` or `/prerelease` (not `/ideas`); both empty → `/allideas` to fill **M58**/**M59** (then **M60**) so `/build` has a `next_row`, or `/maintain`.
4. Name the **next recommended action** in one sentence, then the **industry reason** (link the matching BEST_PRACTICES subsection).
5. Offer a walkthrough of the first 3–4 open rows in `docs/FIRST_30_DAYS.md`, or a 7-day slice if time-boxed. If Week 1 is still open, offer [`TOUR.md`](TOUR.md) before inventing a custom onboarding. For a complete feature dump, offer [`ALLIDEAS.md`](ALLIDEAS.md).
6. Do not dump whole memory files. Do not update `AGENT_MEMORY.md` unless this is a milestone.
7. Home chrome stays **Settings-only**. If a next action would put Theme or donate in the header, send it to Settings/About instead.

Word list: [`GLOSSARY.md`](GLOSSARY.md).

See [`AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md) if your tool has no slash commands.
