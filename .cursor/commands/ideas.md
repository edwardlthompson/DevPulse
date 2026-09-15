# Ideas (backlog, do not implement)

Propose the next in-scope features. This is not `/coach` (next action now), not `/allideas` (uncapped dump), and not `/plan` (implement a chosen feature).

Read @AGENT_MEMORY.md (Persistent Context + latest retrospective only), @BUILD_PLAN.md Sequential lane, @CHANGELOG.md `[Unreleased]`, the latest entries in @DECISION_LOG.md, @docs/FIRST_30_DAYS.md, and @docs/help/BATCH_COMMANDS.md.

1. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`) and `python3 scripts/agent-run.py feedback-inbox`. Summarize stack and the next BUILD_PLAN row in one line. If `fixes` is non-empty, **lead with** “Fix inbox is not empty (N crash/bug issues). Run `/audit` before picking a feature.” Merge `features` (source `github:#N` or `discussion:#N`) into the 5–8 ideas at default P1 unless already on the board.
2. Pick **mode**:
   - **Template** if the stamped project card / purpose still describes this bootstrap template, or `stack` is `multi` on the template repo.
   - **Child** otherwise — read `docs/spec.md` and the active Golden Path README; suggest product slices, not template internals.
3. Print **5–8** ideas that are not already shipped and not already 🔲 on BUILD_PLAN. Each idea: title, **Why** (one sentence + BEST_PRACTICES or industry reason), **Effort** (S or M), **Priority** (P0 / P1 / P2). If the user asked for more than 8 or a complete dump, stop and run @.cursor/commands/allideas.md instead.
4. Cap at 8. No option dump. Name the **single best next** idea in one sentence.
5. **Do not implement.** **Do not edit BUILD_PLAN** unless the user names a number. Then offer: “Say the number to add a 🔲 `[AGENT]` row.”
6. Refuse out of scope: proprietary SDKs on the FOSS path, a second project-generator CLI, a second memory tree, `.agents/agents.md` as project law, Cloud-only defaults, Pages telemetry.
7. **No silent `do all`.** If the user says `do all`, `implement all`, `add all`, or `yes to everything`, do not add rows and do not start `/build`. Restate the numbered list and wait until they name specific numbers (use `/allideas` + `board` only when they asked for an uncapped dump).

Other IDEs: the same recipe is `docs/help/IDEAS.md`. Complete dump: `/allideas` / `docs/help/ALLIDEAS.md`.

Begin now.
