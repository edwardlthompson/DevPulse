# Coach (why)

> Synonym: users may say “why” in chat; this file is the registered `/coach` command.

Read @AGENT_MEMORY.md (Persistent Context + latest retrospective only), @BUILD_PLAN.md Sequential lane, @docs/BEST_PRACTICES.md, and @docs/FIRST_30_DAYS.md.

1. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`). Summarize: active stack, next BUILD_PLAN row, CI line if present.
2. Name the **next recommended action** in one sentence, then the **industry reason** (link the matching BEST_PRACTICES subsection).
3. Offer a walkthrough of the first 3–4 open rows in FIRST_30_DAYS, or a 7-day slice if the user is time-boxed. If Week 1 still has open rows, offer `/tour` (or `docs/help/TOUR.md` in other IDEs) before inventing a custom onboarding. For a backlog of *possible* next features (not the next action now), offer `/ideas` or `docs/help/IDEAS.md`.
4. Do not dump the whole memory file. Do not update AGENT_MEMORY unless this is a milestone.

**Rationale rule:** whenever you create or significantly change a file, add one sentence of why (example: “I’m adding this pre-commit hook because catching style and security issues locally is cheaper than waiting for CI.”).

If the user’s tool has no slash commands, the same walk is `docs/help/COACH.md`.

Begin now.
