---
name: update-deps
description: Local-first dependency scan, dry-run, audit, and patch/minor apply. Use when /update-deps, /ship, or /prerelease.
disable-model-invocation: false
---

# Update dependencies (local-first)

See also: `.cursor/commands/update-deps.md`, `scripts/update-deps.sh`

## Optional depsonar MCP check

When `.cursor/mcp.json` enables depsonar (copy from `.cursor/mcp.foss.example`):

1. Prefer `depsonar_scan` / `depsonar_check` / `depsonar_changelog` / `depsonar_audit` / `depsonar_live_cve`, then `depsonar_update` at patch/minor.
2. If a depsonar tool returns nothing within ~60 seconds, fall back to the CLI below.
3. Quick presence check (does not require a live MCP session): confirm `depsonar` appears in `.cursor/mcp.json` or the example file. Missing MCP is OK — CLI is the supported default.

```bash
# Optional: confirm example ships depsonar
grep -n depsonar .cursor/mcp.json .cursor/mcp.foss.example 2>/dev/null || true

```

## CLI fallback

```bash
python3 scripts/agent-run.py update-deps
python3 scripts/agent-run.py update-deps -- --apply
python3 scripts/agent-run.py update-deps -- --audit

```

Dry-run first. Do not git push. Halt on majors, Kotlin >=2.3.30, or HIGH+ audit findings.

Gradle from depsonar: `python3 scripts/agent-run.py apply-depsonar-gradle -- --pins 'id=ver'` then `--apply`. Never write Kotlin `>=2.3.30`.
