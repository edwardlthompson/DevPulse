---
name: watch-gates-autofix
description: Run watch-agent-gates with autofix in feature scope. Use when /fix or after AGENT BUILD_PLAN steps.
disable-model-invocation: false
---

# Watch gates autofix (3-strike)

See also: `.cursor/commands/fix.md`

**Espresso before emulator reinstall:** on Android 16 `InputManager.getInstance` failures, bump `espresso-core` to ≥3.7.0 (`.cursor/skills/espresso-android16`) before deleting AVDs or re-downloading system images.

Print strike/stage **first**:

```bash
python3 scripts/agent-run.py render-gates-status -- --fix-banner

```

If `strikes >= 3`, halt (`/debug`). Then:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto

```

Exit 1: read `.cursor/agent-progress.json` and gate JSON; fix lint/tests in active feature scope; re-run (max 3 strikes).

Exit 2: halt and escalate per `docs/FOR_AGENTS.md`.

Sprint wrap (all AGENT/AUTO rows ✅): `python3 scripts/agent-run.py smoke-sprint --require` — do not start the next sprint until it passes.

Optional: `python3 scripts/agent-run.py feature-autofix` for mechanical fixers within feature container.
