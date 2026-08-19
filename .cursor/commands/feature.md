# Feature vertical slice step

> Skill: `.cursor/skills/feature-vertical-slice/`

Execute the active BUILD_PLAN feature row only (one feature per task). See @docs/FEATURE_MODULES.md.

When invoked from @.cursor/commands/build.md: execute all open rows for the active feature without stopping; no user prompts.

After each AGENT step:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --step scaffold
```

Use `--step tests` or `--step wire` when appropriate. On exit 2, use `/debug` or escalate.

When the active feature block is fully ✅ and gates pass, read @.cursor/commands/cleanup.md — execute fully.

Begin now.
