# Parallel-agent handoff

> Copy to a gitignored live note (e.g. `.cursor/handoff-<scope>.md`) when `/scope` dispatches. Do **not** commit live copies. Parallel agents never edit `BUILD_PLAN.md`.

## From

- Agent / role:
- Branch: `feature/agent-<task-slug>`

## To

- Agent / role:
- Branch: `feature/agent-<task-slug>`

## Scope prefix

- Isolated path: `examples/{stack}/src/{feature}/` (must match `.cursor/parallel-scope-lock.json`)
- Forbidden: `BUILD_PLAN.md`, `COMPLETED_TASKS.md`, composition roots (`appBootstrap.ts`, `GoldenPathApp.kt`, `main.ts`)

## Acceptance

- 🔲 In-scope files only; `check-parallel-scope` would pass
- 🔲 Unit / view / i18n done for this prefix
- 🔲 Notes for orchestrator (schema gaps, BUILD_PLAN status) — do not edit the board

## Do not

- Edit `BUILD_PLAN.md` or mark Parallel rows ✅ (sequential owner only)
- Touch another agent’s path prefix
- Expand scope without returning to Sequential / Plan Mode
