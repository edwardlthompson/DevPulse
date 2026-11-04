# Feature vertical slice step

> Skill: `.cursor/skills/feature-vertical-slice/`

Execute the active BUILD_PLAN feature row only (one feature per task). See @docs/FEATURE_MODULES.md. View + i18n must meet @docs/ux-ui-guidelines.md (empty/error/loading, one primary CTA, a11y). Definition of Done is not “it renders.”

If `AGENT.md` exists, re-read it before adapting Golden Path About, Settings, purpose, or donate copy — do not ship template stub strings as the product.

After the row is ✅ and gates pass: nudge commit + ask the human to `/push` (never silent `git push`).

On This Computer: only `🔲 [AGENT][LOCAL]` rows (`feature/local-<slug>`). Never claim `[CLOUD]`. Row must include `— scope:`. See @docs/adr/0008-agent-venue.md.

When invoked from @.cursor/commands/build.md: execute all open rows for the active feature without stopping; no user prompts.

After each AGENT step:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto --step scaffold

```

Do not re-run full `validate-bootstrap` mid-slice if watch already passed. Use `--step tests` or `--step wire` when appropriate. On exit 2, use `/debug` or escalate.

When the active feature block is fully ✅, run `python3 scripts/agent-run.py smoke-sprint --require` before the next feature. Then read @.cursor/commands/cleanup.md — execute fully.

Begin now.
