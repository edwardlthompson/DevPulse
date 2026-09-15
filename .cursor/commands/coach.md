# Coach (why)

> Synonym: users may say “why” in chat; this file is the registered `/coach` command.

Read @AGENT_MEMORY.md (Persistent Context + latest retrospective only), @BUILD_PLAN.md Sequential lane, @docs/BEST_PRACTICES.md, and @docs/FIRST_30_DAYS.md.

1. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`) and `python3 scripts/agent-run.py feedback-inbox`. Summarize: active stack, next BUILD_PLAN row, CI line if present. If `fixes.length > 0`, next action is `/audit` and print `Feedback inbox: N crash/bug open — run /audit`. If local compute shows `ollama=up`, mention `docs/LOCAL_MODELS.md`. If this is a child repo, run `python3 scripts/agent-run.py check-template-updates`; if it prints a newer template version, offer `/upgrade` (gap plan only — do not overwrite the app).
2. Compare dirty Unreleased vs empty board from that snapshot (`open_agent_auto` in `build-sprint-status --json`, and `[Unreleased]` list items):
   - `open_agent_auto > 0` → next action is `/build` (name `next_row` from the active milestone, e.g. **M59**). If Unreleased is dirty, say notes are waiting — **do not** recommend `/ship` until AGENT/AUTO rows are done.
   - `open_agent_auto == 0` and Unreleased dirty → next action is `/ship` (or `/prerelease` if they are not ready to push). **Do not** lead with `/ideas` or `/allideas`.
   - both empty → `/allideas` to fill the next milestone board (**M58** ship leftovers / **M59** CI+stacks / then **M60**) so `/build` has a `next_row`. Prefer `/allideas` over a short `/ideas` dump when the maintainer board is empty.
3. Name the **next recommended action** in one sentence, then the **industry reason** (link the matching BEST_PRACTICES subsection).
4. Offer a walkthrough of the first 3–4 open rows in FIRST_30_DAYS, or a 7-day slice if the user is time-boxed. If Week 1 still has open rows, offer `/tour` (or `docs/help/TOUR.md` in other IDEs) before inventing a custom onboarding. For a ranked backlog of *possible* next features (not the next action now), offer `/ideas` or `docs/help/IDEAS.md`. For a complete dump to fill BUILD_PLAN, offer `/allideas` or `docs/help/ALLIDEAS.md`.
5. Do not dump the whole memory file. Do not update AGENT_MEMORY unless this is a milestone.

**Rationale rule:** whenever you create or significantly change a file, add one sentence of why (example: “I’m adding this pre-commit hook because catching style and security issues locally is cheaper than waiting for CI.”).

If the user’s tool has no slash commands, the same walk is `docs/help/COACH.md`.

Begin now.
