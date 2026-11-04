# /fix — Gate autofix loop

Follow `AGENTS.md` strictly.

1. Read `.cursor/commands/fix.md` (and `docs/FEATURE_MODULES.md` autofix rules).
2. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`.
3. Fix within feature scope only; 3-strike then halt and escalate.
4. Do not disable gates or skip hooks.

Reference: `.cursor/commands/fix.md`.
