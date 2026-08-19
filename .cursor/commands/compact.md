# Save session checkpoint

Before clearing chat, write `.cursor-session-state.json` from current context:
- repo mode (Bootstrap vs Reference)
- active stack from `.cursor/stack-selection.json`
- current BUILD_PLAN sprint and sequential step
- brief note of in-progress feature path
- `destructive_ops_approved`: include `["git push"]` when user invoked `/push` or `/ship` (allows Cursor shell hook to permit push)

Use schema in `.cursor-session-state.example.json`. Do not commit this file (gitignored).

Begin now.
