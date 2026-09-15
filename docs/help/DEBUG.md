# Debug (any IDE)

Ask your agent to investigate a defect with evidence first. In Cursor you can type `/debug` instead.

This is not a full-repo review (`/audit`). It is gate/CI triage.

## Paste prompt

```
Read docs/help/DEBUG.md and investigate the current failure. Do not start a full-repo audit.

```

## Recipe

1. If `.cursor/last-feature-gate.json` exists, read it first. Print `failed_stage`, `exit_code`, `human_hint`, and `log_tail`. Do not start a free-form hunt while this file explains the failure.
2. Read `.cursor/agent-progress.json` `strikes` (default 0 if missing). If `strikes >= 3`: halt. Summarize evidence. Do not propose a fourth mechanical fix. Escalate to the human.
3. If `last-feature-gate.json` is missing or not JSON: collect runtime evidence (command output, CI log URL, repro steps).
4. Check `KNOWLEDGE_BASE.md` and `docs/FOR_AGENTS.md` Failure Playbook.
5. Confirm repro locally before editing code. When root cause is identified, switch to Agent Mode (or `/fix`).

See [`AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md) if your tool has no slash commands.
