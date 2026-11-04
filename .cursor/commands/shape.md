# /shape — idea → spec (coach mode)

Turn a rough idea into a short, buildable spec. Write **NO** application code.
Twin: `docs/help/SHAPE.md`. Specs live under `docs/specs/`.

## 0. Size gate

- TRIVIAL (obvious, ≤ ~3 files): say "Small enough — want me to just do it?" and stop.
- Several features: propose the split, shape only the first.
- User said `skip` / `defaults`: ask nothing; write the spec with flagged assumptions.

## 1. Look first

Skim README, BUILD_PLAN.md, and files this idea touches.
**Must read** `docs/PREFERENCES.md` (coding patterns, workflow, and vibe-coding security risks).
Never ask what the code or preferences already answer.
Report ONE line: what you found that matters.

## 2. Mirror

"Here's what I heard: <1–2 sentences>." Invite correction.

## 3. One batch, max 5 questions

Only questions whose answers would change the build. Skip any already clear.

**Format each question with blank lines between options** (never cramped `a/b/c` on one line):

```text
1. · <plain question>
   · Why it matters: <≤8 words>

   a) <option>
   b) <option> ★ recommended
   c) <option>

   or tell me
```

Cover, in this order:

1. Trigger + happy path
2. "Done looks like"
3. Out of scope
4. Must-not-break / constraints
5. One concrete example

End with: `Reply like \`1a 2b 3 skip\` — or say \`defaults\`.`

`/shape deep` = one question at a time instead.

## 4. Vague answers

Don't re-ask. Offer two concrete phrasings; I pick one.

## 5. Write docs/specs/{slug}.md (≤ 40 lines)

Goal · Trigger/users · Requirements · Out of scope · Acceptance checks ·
Assumptions I made (flagged) · Open questions · Files likely touched.
Behavior only (what/why). No implementation unless I specified it.
Use `docs/specs/_TEMPLATE.md` as the skeleton.

## 6. Hand off, then STOP

- Add one BUILD_PLAN.md `[AGENT][LOCAL]` row linking the spec.
- Print: spec path, 3-line summary, and:
  "Edit the spec if anything's off. To build: new chat → `Implement @docs/specs/{slug}.md`
  (cheaper model is fine)."
- Offer to append any new durable preference to docs/PREFERENCES.md (ask first).

Begin now.
