# Coach (any IDE)

Ask your agent what to do next and why. In Cursor you can type `/coach` instead.

This is not a backlog (`/ideas` / [`IDEAS.md`](IDEAS.md)). It is the next action now.

## Paste prompt

```
Read docs/help/COACH.md and tell me the next recommended action and why. Do not implement unless I ask.
```

## Recipe

1. Read `AGENT_MEMORY.md` (Persistent Context + latest retrospective only), `BUILD_PLAN.md` Sequential, `docs/BEST_PRACTICES.md`, and `docs/FIRST_30_DAYS.md`.
2. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`). Summarize stack, repo mode (template vs child), next BUILD_PLAN row, and any dirty Unreleased / unpushed note.
3. Name the **next recommended action** in one sentence, then the **industry reason** (link the matching BEST_PRACTICES subsection).
4. Offer a walkthrough of the first 3–4 open rows in `docs/FIRST_30_DAYS.md`, or a 7-day slice if time-boxed. If Week 1 is still open, offer [`TOUR.md`](TOUR.md) before inventing a custom onboarding.
5. Do not dump whole memory files. Do not update `AGENT_MEMORY.md` unless this is a milestone.

Word list: [`GLOSSARY.md`](GLOSSARY.md).

See [`AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md) if your tool has no slash commands.
