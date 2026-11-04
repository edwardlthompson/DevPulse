---
name: validate-bootstrap
description: Run local bootstrap validation gates. Use when /gates, Sprint 0 sign-off, or pre-push local check.
disable-model-invocation: false
---

# Validate bootstrap (local gates)

See also: `.cursor/commands/gates.md` · ADR-0009

```bash
# Pre-commit / mid-slice (core checks)
python3 scripts/agent-run.py validate-bootstrap --agent

# Maintainer local (~70 checks; skips GitHub API action resolve)
python3 scripts/agent-run.py validate-bootstrap --quick

python3 scripts/agent-run.py check-repo-hygiene

```

`/gates` defaults to `--agent` + dirty stacks; `/gates --full` uses `--quick` + multi. Do not re-run full validate mid-slice if `watch-agent-gates` already passed.
