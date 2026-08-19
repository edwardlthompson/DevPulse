---
name: sprint0-signoff
description: Run canonical Sprint 0 sign-off gate chain. Use after init/setup or Child Repo Playbook sign-off.
disable-model-invocation: false
---

# Sprint 0 sign-off

See also: `.cursor/commands/gates.md`, `BUILD_PLAN.md` Child Repo Playbook

From repo root, all green on `main` (or active branch before merge):

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py feature-gate --stack "$(python3 -c "import json;print(json.load(open('.cursor/stack-selection.json')).get('stack','multi'))" 2>/dev/null || echo multi)"
python3 scripts/agent-run.py check-github-ci -- --wait 300
python3 scripts/agent-run.py check-license-compliance
```

Do not mark Sprint 0 BUILD_PLAN rows complete while any step is red. Fix in scope, re-run.
