---
name: ux-review
description: Score shipped UI against construction law and write BUILD_PLAN UX inventory items. Use when /ux-review, /ux-apply, or a UX-NNN inventory pass.
disable-model-invocation: false
---

# UX review / apply

See also: `.cursor/commands/ux-review.md`, `.cursor/commands/ux-apply.md`, `docs/help/UX-REVIEW.md`, `docs/help/UX-APPLY.md`, `docs/ux-ui-guidelines.md`

1. Construction law is `docs/ux-ui-guidelines.md` (always-on `.cursor/rules/ux-ui.mdc`). `/ux-review` uses that same law.
2. Inspect real UI only. Template: Golden Path web+Android. Child: `AGENT.md` + active stack. No UI → 0 items.
3. Write every **change** to `<!-- ux-inventory -->` in the same turn (`planned` / `in_progress` / `done` only).
4. `/ux-apply UX-NNN` implements one id, then scoped review. `/build` does not auto-drain inventory.
5. Do not `git push` unless the user invoked `/push` or `/ship`.
