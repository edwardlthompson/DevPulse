# /ship — Pre-release and publish path

Follow `AGENTS.md` and destructive-ops rules strictly.

1. Run local dep update: `python3 scripts/agent-run.py update-deps` (dry-run first unless human approved apply).
2. Follow the project pre-release / ship path (`docs/help/BATCH_COMMANDS.md`, `.cursor/commands/ship.md` if present).
3. Surface every `[HUMAN]` gate clearly; do not `git push` without explicit approval (`/ship` or `/push` grants it in Cursor).
4. Stop on the first blocking failure and report it.

Reference: `docs/help/BATCH_COMMANDS.md`.
