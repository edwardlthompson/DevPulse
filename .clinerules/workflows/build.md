# /build — Execute next BUILD_PLAN work

Follow `AGENTS.md` strictly. `/build` and `/cleanup` run together when a block finishes.

1. Read the next open `[AGENT]` (or `[AGENT][LOCAL]`) row in `BUILD_PLAN.md`.
2. If multi-file or architectural, stay in Plan mode until the human approves.
3. Execute only that row (smallest coherent slice). Prefer targeted reads.
4. After every code change, run the smallest relevant verification (`python3 scripts/agent-run.py verify` or stack tests).
5. Update `.agent/session-state.md` / mark the row when done. Do not invent new scope.
6. Same error or same tool call three times -> stop and ask the human.
7. When the active sprint/feature block is all ✅: run smoke → gates → **mandatory `/cleanup`** (read `docs/help` twin or `.cursor/commands/cleanup.md`) → print slim BUILD_PLAN (**Now:** + remaining tally). Never skip cleanup after a completed block.
8. Full Cursor recipe: `.cursor/commands/build.md`.

Reference: `docs/help/BATCH_COMMANDS.md`, `.cursor/commands/build.md` if present.
