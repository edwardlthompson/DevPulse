---
name: canvas-bootstrap-status
description: Render Sprint 0 / gate status as a Canvas (or markdown table fallback). Use when /gates status overview is needed.
disable-model-invocation: false
---

# Canvas bootstrap status

See also: `.cursor/commands/gates.md`

1. Read `.cursor/stack-selection.json` for stack and `distribution_tier`.
2. Run:

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py check-repo-hygiene
```

3. Prefer a **Canvas** (interactive React artifact beside chat) showing: stack, tier, each gate pass/fail, next BUILD_PLAN open row.
4. If Canvas tooling is unavailable, fall back to a markdown gate table in chat — do not fail the skill.
