# Feature or ADR plan (Plan Mode)

Read @docs/CURSOR_MODES.md and the active BUILD_PLAN row.

## Autonomous mode (invoked from `/build`)

When called from @.cursor/commands/build.md:

- **Do not ask the user for approval** — pick the single best approach and execute.
- **Do not present multiple options** — use internal ### Critique + ### Parallelization only.
- Proceed directly to implementation after a brief internal plan.

## Standalone `/plan` (interactive)

If the trivial rubric says Agent, skip planning and execute directly.
Otherwise pick the **single best approach** and present it with a mandatory resolved `### Critique` before coding.

### Resolved Critique (required)

Include `### Critique` as an **Issue → Resolution** table. Do this proactively — never wait for the human to ask about risks.

Rules:

1. **Every issue has a resolution** — verb + artifact (file, test, gate, BUILD_PLAN step, or explicit `[HUMAN]` destructive-ops item with owner).
2. **Resolutions appear in the plan body** — todos/steps implement them; not critique-only footnotes.
3. **No open questions in Critique** — resolve architectural choices in the plan before presenting. Ask the human only for `[HUMAN]` destructive-ops or facts undiscoverable from the codebase.
4. **No bare “defer” / “monitor” / “optional later”** unless the resolution names a tracked follow-up (`BUILD_PLAN` row, `DECISION_LOG` entry, or dated issue) and states why it is safe to ship without it now.
5. **Checklist minimum** (null/empty, timeouts, races, unhandled exceptions) — each row **Resolved** with mitigation or **N/A** with one-line why.
6. **Single chosen approach** — use Critique to justify and harden the path, not to dump unresolved alternatives.

```markdown
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty input at boundary | Validate with schema X; reject with typed error Y; test case Z |
| Network timeout on fetch | AbortSignal 10s; offline mock path; user-visible retry in feature F |
```

When drafting or extending **BUILD_PLAN.md** sprints, include mandatory **### Parallelization** (alongside ### Critique):

1. **Sequential lock list** — shared schema/types/API that must finish before Parallel (1–3 items max)
2. **Decomposition table** — Task | Isolated scope | Why safe in parallel
3. **`agent_count_target`** — integer; justify any target `< 2` in one sentence
4. **Dry-run** — expected output of `python3 scripts/agent-run.py plan-parallel-dispatch --draft BUILD_PLAN.md --suggest`

Apply the decomposition checklist in @BUILD_PLAN.md (multi-stack, logic/view split, tests/docs/CI). **Maximize agent_count** across non-overlapping scopes.

Before asking human approval of BUILD_PLAN changes (standalone `/plan` only), run:

```bash
python3 scripts/agent-run.py check-build-plan-parallel
```

Do not edit code until the user approves the plan **unless** autonomous `/build` invoked this command.

Begin now.
