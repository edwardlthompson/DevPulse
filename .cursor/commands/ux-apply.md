# UX apply (one inventory item)

Implement **one** BUILD_PLAN UX inventory id to [`docs/ux-ui-guidelines.md`](../../docs/ux-ui-guidelines.md). Then scoped review; append new findings.

> Skill: `.cursor/skills/ux-review/`

Other IDEs: `docs/help/UX-APPLY.md`.

1. Require an id (`UX-014` or the title). If missing, list `planned` items and stop.
2. Set that item **Status:** `in_progress`. Do not drain the rest of the inventory. `/build` does not auto-run UX-NNN.
3. Ship the **smaller** change that meets the law (tokens from `docs/DESIGN_GUIDE.md`; empty/error/loading; one primary CTA; a11y). Record any remaining larger change as a new `UX-NNN`.
4. Tests or fallback in `docs/features/` as for `/feature`. Then:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto

```

5. Set the item **Status:** `done` when Success is met. Run a **scoped** `/ux-review` on the same surface; append new ids in this turn.
6. Do not `git push` unless the user invoked `/push` or `/ship`.

Begin now.
