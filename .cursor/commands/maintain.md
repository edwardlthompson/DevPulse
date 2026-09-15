# Weekly maintenance super workflow

Read and execute each sub-command in order. After each step, summarize pass/fail.

0. Run `python3 scripts/agent-run.py sync-open-prs-build-plan -- --apply` — mirror open Dependabot / Release Please PRs into BUILD_PLAN
1. Run `python3 scripts/agent-run.py cursor-feature-radar` — summarize top 3 new scored URLs from `CURSOR_RADAR_REPORT.md` (no auto-action)
2. Read @.cursor/commands/triage.md — execute fully
3. Read @.cursor/commands/update-deps.md — execute fully (local scan/audit/apply)
4. Read @.cursor/commands/dependabot.md — execute fully (leftover GitHub alerts/PRs)
5. Read @.cursor/commands/audit.md — execute fully

Begin now.
