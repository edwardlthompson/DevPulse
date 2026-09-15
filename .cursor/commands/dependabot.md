# Dependabot leftover triage (GitHub backup)

Local updates already ran via `/update-deps` (or run that first if you skipped it).

First sync open Dependabot / Release Please PRs onto the BUILD_PLAN managed block:

```bash
python3 scripts/agent-run.py sync-open-prs-build-plan -- --apply

```

Follow @docs/SECURITY_TRIAGE.md and @KNOWLEDGE_BASE.md KB-007.
List remaining open Dependabot alerts and PRs via `gh`; prioritize Critical/High.
Merge leftover safe bumps; use npm `overrides` in `examples/web/package.json` for transitive CVEs when needed.
Document temporary overrides in @DECISION_LOG.md.

GitHub Dependabot is weekly backup only. Day-to-day bumps use `python3 scripts/agent-run.py update-deps`.

Begin now.
