---
name: parallel-scope
description: Build parallel agent manifest and dispatch scopes. Use when /scope or after Sequential lock in BUILD_PLAN.
disable-model-invocation: false
---

# Parallel scope dispatch

See also: `.cursor/commands/scope.md`

```bash
python3 scripts/agent-run.py plan-parallel-dispatch --require-sequential-clear --json
python3 scripts/agent-run.py check-parallel-scope
```

Write manifest to `.cursor/parallel-scope-lock.json`. When `agent_count >= 2`, launch named subagents (`gate-fixer`) per scope.md — one message, concurrent tasks on **This Computer** (local-first; do not default to Cloud Agents).

Optional: `python3 scripts/agent-run.py setup-agent-worktrees` or native `/worktree` / `/best-of-n` for hard isolation.

Forbidden paths: `BUILD_PLAN.md`, composition roots — see `docs/PARALLEL_AGENT_SCOPES.md`.
