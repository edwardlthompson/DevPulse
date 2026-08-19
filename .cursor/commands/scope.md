# Parallel dispatch (manifest + auto Task launch)

> Skill: `.cursor/skills/parallel-scope/`

Read @docs/PARALLEL_AGENT_SCOPES.md and the active BUILD_PLAN Parallel table.

## 1. Preconditions

- Sequential schema-lock steps for the active sprint/feature are complete.
- Run:

```bash
python3 scripts/agent-run.py plan-parallel-dispatch --require-sequential-clear --json

```

If blockers include open Sequential items, finish them first.

## 2. Manifest and scope lock

```bash
python3 scripts/agent-run.py plan-parallel-dispatch --json

```

Write the manifest agents array to `.cursor/parallel-scope-lock.json` (gitignored):

```json
{
  "created_at": "<ISO8601>",
  "agent_count": 2,
  "agents": [ { "id", "task", "scope", "branch", "forbidden_paths" } ]
}

```

Print **agent_count** in one line for the user.

## 3. Auto-dispatch rules

| agent_count | Action |
|-------------|--------|
| 0 | Run `python3 scripts/agent-run.py plan-parallel-dispatch --suggest`. Orchestrator adds suggested rows to BUILD_PLAN Parallel table, then re-run manifest **once**. If still 0, document `<!-- parallel_exception: reason -->` or escalate. |
| 1 | Execute the task inline (no Task tool). |
| 2–8 | **One assistant message, N concurrent Task tool calls** using custom subagent **`gate-fixer`** (`run_in_background: true`). For Plan/decompose only, prefer **`explorer`** (readonly). **Local-first:** run these on This Computer (not Cloud) so all cores work the scopes in parallel. |
## 4. Subagent prompt template

Use **`.cursor/agents/gate-fixer.md`** (or Task with matching prompt). Each subagent must receive:

- Read `.cursor/parallel-scope-lock.json` — stay inside assigned `scope` only.
- **Forbidden paths:** `BUILD_PLAN.md`, `COMPLETED_TASKS.md`, composition roots (`appBootstrap.ts`, `GoldenPathApp.kt`, `main.ts`). Do not edit these; return notes to orchestrator instead.
- Optional: copy `docs/features/_handoff.md` → gitignored `.cursor/handoff-<scope>.md` (from/to, scope, acceptance). Do not edit BUILD_PLAN.
- Branch: `feature/agent-<task-slug>` (document in commit messages if branch not checked out).
- Task: BUILD_PLAN Parallel row description.
- After work: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --step tests` or `--step wire` as appropriate.
- Report completion: `python3 scripts/agent-run.py agent-progress set-step --name tests` (or `view`, `e2e`, matching the task).

## 5. Orchestrator merge

1. Wait for all subagents to complete.
2. Resolve conflicts if any (sequential owner only).
3. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix`.
4. Mark Parallel rows ✅ in BUILD_PLAN (Parallel agents never edit BUILD_PLAN).
5. Delete or archive `.cursor/parallel-scope-lock.json` when done.
6. If the sprint block is fully ✅, read @.cursor/commands/cleanup.md — execute fully.

Optional hard isolation: `python3 scripts/agent-run.py setup-agent-worktrees` (see @docs/PARALLEL_AGENT_SCOPES.md).

Native Cursor worktrees (Agents Window / `/worktree` / `/best-of-n`) use `.cursor/worktrees.json` + OS setup scripts — distinct from parallel-lock worktrees. For flaky gate comparison across models, prefer `/best-of-n`.

Begin now.
