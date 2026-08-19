---
name: validate-bootstrap
description: Run local bootstrap validation gates. Use when /gates, Sprint 0 sign-off, or pre-push local check.
disable-model-invocation: false
---

# Validate bootstrap (local gates)

See also: `.cursor/commands/gates.md`

Run from repo root:

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py check-repo-hygiene
python3 scripts/agent-run.py feature-gate --stack "$(python3 -c "import json;print(json.load(open('.cursor/stack-selection.json')).get('stack','multi'))" 2>/dev/null || echo multi)"
```

On failure: fix in scope, re-run. Do not mark BUILD_PLAN rows complete while gates are red.
