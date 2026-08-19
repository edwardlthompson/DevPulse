---
name: check-repo-hygiene
description: Run repo hygiene gate (tracked vs ignored paths). Use when /gates, /audit, or pre-push hygiene check.
disable-model-invocation: false
---

# Check repo hygiene

See also: `.cursor/commands/gates.md`, `.cursor/commands/audit.md`

Run from repo root:

```bash
python3 scripts/agent-run.py check-repo-hygiene
```

On failure: remove or gitignore ephemeral paths; do not commit `node_modules/`, `.env`, or live configs. Re-run until green.
