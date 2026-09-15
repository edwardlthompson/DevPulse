---
name: adr
description: Write one Architecture Decision Record under docs/adr/. Use when /adr or a milestone decision.
disable-model-invocation: false
---

# Architecture Decision Record

See also: `.cursor/commands/adr.md`, `docs/help/ADR.md`

1. Read `docs/adr/` and pick the next `NNNN`. Do not reuse a number.
2. Write `docs/adr/NNNN-short-slug.md` with Status, Date, Deciders, Context, Decision, Alternatives considered, and Consequences.
3. Include a resolved `### Critique` Issue→Resolution table.
4. Keep the file under 150 lines. Do not invent a second memory tree.
5. Do not `git push` unless the user invoked `/push` or `/ship`.
