# Architecture Decision Record

> Skills: `.cursor/skills/adr/`

Record one decision in `docs/adr/`. This is not `/plan` (feature approach) and not `/coach` (what next).

Other IDEs: the same recipe is `docs/help/ADR.md`.

1. Read `docs/adr/` and pick the next `NNNN` (`0003` after `0002`). Do not reuse a number.
2. Write `docs/adr/NNNN-short-slug.md` with Status, Date, Deciders, Context, Decision, Alternatives considered, and Consequences.
3. Include a resolved `### Critique` Issue→Resolution table (null/empty, timeouts, races, unhandled exceptions).
4. Keep the file under 150 lines. Do not invent a second memory tree — link `DECISION_LOG.md` or `AGENT_MEMORY.md` only when this is a milestone.
5. Do not `git push` unless the user invoked `/push` or `/ship`.

Begin now.
