# All ideas (uncapped backlog dump, do not implement)

Also invoked as `/AllIdeas` or the bare word `allideas`. This is not `/ideas` (ranked 5–8), not `/coach` (next action now), and not `/plan` (implement a chosen feature). Use this when the user wants a **complete** in-scope dump to fill BUILD_PLAN for sizeable `/build` progress.

Read @AGENT_MEMORY.md (Persistent Context + latest retrospective only), @BUILD_PLAN.md Sequential lane (Template Maintainer board + child playbook), @CHANGELOG.md `[Unreleased]`, the latest entries in @DECISION_LOG.md, @docs/FIRST_30_DAYS.md, and @docs/help/BATCH_COMMANDS.md.

1. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`) and `python3 scripts/agent-run.py feedback-inbox`. Summarize stack and the next BUILD_PLAN row in one line. If `fixes` is non-empty, **lead with** “Fix inbox is not empty (N crash/bug issues). Run `/audit` before picking a feature.” Merge `features` (source `github:#N` or `discussion:#N`) into the dump at default P1 unless already on the board.
2. Pick **mode**:
   - **Template** if the stamped project card / purpose still describes this bootstrap template, or `stack` is `multi` on the template repo.
   - **Child** otherwise — read `docs/spec.md` and the active Golden Path README; suggest product slices, not template internals.
3. Print **every** in-scope idea that is not already shipped and not already 🔲/`✅` on BUILD_PLAN. Group by theme. Each idea: number, title, **Why** (one sentence + BEST_PRACTICES or industry reason), **Effort** (S or M), **Priority** (P0 / P1 / P2).
4. **No cap.** If the dump will exceed ~80 items, print the first 80, then say “Say `continue` for the rest.” Name the **single best next** idea in one sentence.
5. **Do not implement.** **Do not edit BUILD_PLAN** unless the user names numbers, says `board` / `add all` / `do all`. Then add one 🔲 `[AGENT]` (or `[HUMAN]` when the item is credentials/DPIA/badge-login) row per idea under the Template Maintainer board (template) or the active child sprint (child). Skip duplicates of existing leftover HUMAN/ADB rows.
6. Refuse out of scope: proprietary SDKs on the FOSS path, a second project-generator CLI, a second memory tree, `.agents/agents.md` as project law, Cloud-only defaults, Pages telemetry, a live crash-proxy without DPIA.

Other IDEs: the same recipe is `docs/help/ALLIDEAS.md`. Ranked short list: `/ideas` / `docs/help/IDEAS.md`.

Begin now.
