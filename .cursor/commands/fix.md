# Gate autofix (feature scope)

> Skill: `.cursor/skills/watch-gates-autofix/`

Autonomous feature step with auto-fix.

## Android 16 / Espresso first

If instrumented Compose fails with `InputManager.getInstance` (or similar API 36 reflection), **prefer bumping Espresso to 3.7.0+** (`/.cursor/skills/espresso-android16/`) before reinstalling the emulator image or wiping an AVD. Reinstall is last resort after the pin + `check-espresso-android16` pass.

## Step 0 — Print strike and stage first

Run this **before** the gate loop. Do not skip it.

```bash
python3 scripts/agent-run.py render-gates-status -- --fix-banner

```

Prints `strikes=N` and `failed_stage=…` from `.cursor/agent-progress.json` and `.cursor/last-feature-gate.json`. If `strikes >= 3`, halt and run `/debug` — do not autofix.

## Step 1 — Gate loop

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto --step scaffold

```

If exit 1: read `.cursor/agent-progress.json` and gate JSON; fix lint/tests in active feature scope; re-run.
On exit 2 (3-strike), halt and switch to Debug Mode or escalate to human.
Push to remote still requires `/push`, `/ship`, or explicit user approval.

Begin now.
