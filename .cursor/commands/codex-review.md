# Codex third-party review (local)

> Skill: `.cursor/skills/codex-review/`
> Docs: @docs/CODEX_REVIEW.md

Peer to `/audit` for model-diverse review. Codex is **read-only** — you apply repairs via BUILD_PLAN + `/fix`.

## Steps

1. Run:

```bash
python3 scripts/agent-run.py run-codex-review
```

- Exit `0`: wrote gitignored `CODE_REVIEW.md` (may have zero findings).
- Exit `3`: `SKIP: Codex review (no key/CLI)` — tell the human; do not invent findings.
- Exit `1`: parse/CLI failure — leave prior `CODE_REVIEW.md` untouched; halt this command.

2. If Critical/High findings exist in `CODE_REVIEW.md`, append a Sequential sprint to @BUILD_PLAN.md with one 🔲 `[AGENT]` row per Critical/High (link `F-00N`). Medium may be batched.

3. Implement AGENT rows top-to-bottom. After each:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --step none
```

4. Stop at 3-strike / exit `2`. Do not call Codex again to patch.

Begin now.
