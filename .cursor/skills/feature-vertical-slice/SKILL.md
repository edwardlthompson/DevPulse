---
name: feature-vertical-slice
description: Implement one BUILD_PLAN feature row as a vertical slice. Use when /feature or Sprint 2+ feature work.
disable-model-invocation: false
---

# Feature vertical slice

See also: `.cursor/commands/feature.md`, `docs/FEATURE_MODULES.md`

1. Execute **one** open BUILD_PLAN feature row only (logic, view, tests, i18n in one container).
2. Thin wiring in composition root (≤10 lines).
3. After each AGENT step:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix
```

On exit `2`, switch to Debug Mode or escalate. Do not batch multiple feature rows.
