# UX review (same construction law)

> Skill: `.cursor/skills/ux-review/`

Score **shipped** UI against [`docs/ux-ui-guidelines.md`](../../docs/ux-ui-guidelines.md). This is not a second checklist. Do **not** invent screens. Do **not** implement unless the user also asked `/ux-apply`.

Other IDEs: `docs/help/UX-REVIEW.md`.

## Scope

- **Template repo:** Golden Path `examples/web` and `examples/android` (skip a stack if pruned).
- **Child:** `AGENT.md` + active stack. No UI stack → **0 items**; do not invent screens.
- Flags: `--quick` (skip live compare fetch), `--compare` (named products), `--a11y` (WCAG walk first), or a scoped flow name.

## Procedure

1. Read `docs/ux-ui-guidelines.md` and `.cursor/rules/ux-ui.mdc`. Tokens/chrome: `docs/DESIGN_GUIDE.md`.
2. Inspect **real** UI (routes, views, copy, tokens). Walk first-time and returning flows.
3. Name 3–5 comparable products only if `--compare` or not `--quick`. Network timeout → local findings still recorded.
4. Every **change** (including removals and WCAG 2.2 AA failures / AAA opportunities) becomes a `UX-NNN` inventory item **in this turn**. Merge by id. Status `planned` only. Forbidden: backlog, deferred, later, phase-2, optional.
5. “Protect what’s working” stays in the report unless a change is required to keep it working.
6. Do not start `/build` on inventory items. Promote with `/ux-apply UX-NNN` (or a Sequential `[AGENT]` row).

## Inventory heading

```markdown
### UX-NNN — Title
- **Status:** planned
- **Source:** /ux-review
- **Severity:** High | Medium | Low
- **Effort:** S | M | L
- **Impact:** 1–10
- **Where:** screen / file
- **What's wrong:** …
- **Why:** …
- **Fix:** …
- **Pattern:** named product or guideline section
- **Success:** observable
```

Replace `_No UX inventory items._` when adding the first id. Next id is `UX-001` if empty, else one past the highest `UX-NNN`.

`--quick` only speeds the audit. Record everything you find.

Begin now.
