# Gates

> Skills: `.cursor/skills/validate-bootstrap/`, `.cursor/skills/check-repo-hygiene/`, `.cursor/skills/canvas-bootstrap-status/`

Default = agent-fast + dirty stacks. Pass `--full` for today’s multi-stack release wave.

```bash
python3 scripts/agent-run.py check-local-compute
python3 scripts/agent-run.py validate-bootstrap --agent
python3 scripts/agent-run.py check-cursor-hooks -- --smoke
python3 scripts/agent-run.py watch-agent-gates --once --scope auto
python3 scripts/agent-run.py smoke-sprint --if-complete
python3 scripts/agent-run.py check-repo-hygiene
python3 scripts/agent-run.py render-gates-status
python3 scripts/agent-run.py summarize-coverage

```

**`--full`:**

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py feature-gate --stack multi
python3 scripts/agent-run.py smoke-sprint --if-complete
python3 scripts/agent-run.py check-repo-hygiene
python3 scripts/agent-run.py run-android-emulator-local -- --if-device
python3 scripts/agent-run.py render-gates-status
python3 scripts/agent-run.py summarize-coverage

```

Report `OK gates` or `FAIL <name> — fix <action>`. Then canvas status overview.

Begin now.
