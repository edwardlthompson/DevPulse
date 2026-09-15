# Architecture Decision Record (any IDE)

Ask your agent to record one decision under `docs/adr/`. In Cursor you can type `/adr` instead.

This is not `/plan` (feature approach) and not `/coach` (what next).

## Paste prompt

```
Read docs/help/ADR.md and write the next numbered ADR. Do not push.

```

## Recipe

1. Read `docs/adr/` and pick the next `NNNN` (`0003` after `0002`). Do not reuse a number.
2. Write `docs/adr/NNNN-short-slug.md` with Status, Date, Deciders, Context, Decision, Alternatives considered, and Consequences.
3. Include a resolved `### Critique` Issue→Resolution table.
4. Keep the file under 150 lines. Do not invent a second memory tree.
5. Do not `git push` unless the user invoked `/push` or `/ship`.
