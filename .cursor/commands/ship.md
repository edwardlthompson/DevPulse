# Publish release super workflow

Invoking this command grants explicit approval for `git push` per destructive-ops rules. When running `/compact`, set `"destructive_ops_approved": ["git push"]` in session state for Cursor shell hooks.

Read and execute each sub-command in order. After each step, summarize pass/fail.

1. Read @.cursor/commands/update-deps.md — execute fully (dry-run, audit, apply patch/minor). Do **not** `git push` in this step.
2. Read @.cursor/commands/prerelease.md — execute fully (deps already updated: audit-only + Release Please dry-run + autofix + `pre-release-gate.sh --local`)
3. Read @.cursor/commands/push.md — execute fully
4. Read @.cursor/commands/regress.md — execute fully (full GitHub pre-release-gate)

Begin now.
