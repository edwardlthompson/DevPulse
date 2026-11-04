# /compact — Externalize context

Follow `AGENTS.md` strictly.

1. Update `.agent/session-state.md` (copy from `.agent/session-state.md.example` if needed): current BUILD_PLAN row, last 3 decisions/questions, files touched, last verification result.
2. Optionally run `python3 scripts/agent-run.py compress-memory` and show the paste block.
3. For Cursor JSON checkpoints, also follow `/compact` / `compact-session-state`.
4. Complements Cline built-in `/smol` — do not replace it.

Reference: `docs/help/SESSION_START.md`.
