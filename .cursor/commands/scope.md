# Parallel dispatch

> Skill: `.cursor/skills/parallel-scope/`

Venue: LOCAL only on This Computer. See `docs/adr/0008-agent-venue.md`.

```bash
python3 scripts/agent-run.py check-parallel-scope -- --dry-run
python3 scripts/agent-run.py plan-parallel-dispatch --require-sequential-clear --json
python3 scripts/agent-run.py plan-parallel-dispatch --json

```

Write `.cursor/parallel-scope-lock.json`, then launch one Task per agent (non-overlapping scopes). Details: `docs/PARALLEL_AGENT_SCOPES.md`.

When done: `python3 scripts/agent-run.py gc-parallel-lock` (removes empty/invalid/24h-stale locks; keeps a stale lock if `git status` is still dirty under an agent `scope`).

Begin now.
