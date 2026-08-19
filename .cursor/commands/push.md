# Release: commit, push, and update docs

Framework: AGENT/HUMAN/ADB/AUTO; semver via Release Please / `.template-version`.
**User invoked `/push` — explicit approval for `git push origin main`.** When running `/compact` before push, set `"destructive_ops_approved": ["git push"]` in session state so `.cursor/hooks` permits the push.

## Step 1 — Pre-release validation

- **Child repo:** `validate-bootstrap.sh --quick`, `feature-gate.sh --stack <active>`, `check-license-compliance.sh` after locked installs
- **Template maintainer:** also `run-maintainer-gates.sh` and `pre-release-gate.sh` per @docs/MAINTAINING_THE_TEMPLATE.md
- Verify @README.md via `check-readme-health.sh`
- Update @CHANGELOG.md `[Unreleased]`

## Step 2 — Release notes

Create/update RELEASE_NOTES.md from CHANGELOG, BUILD_PLAN rows, recent commits (use @RELEASE_NOTES.md.example).

## Step 3 — Commit and push

- Stage **explicit paths only** (never `git add .`)
- Commit: `chore(release): prepare vX.Y.Z release` with key changes in body
- `git push origin main`
- `python3 scripts/agent-run.py check-github-ci --wait 600`
- Zero open Critical/High Dependabot alerts

## Step 4 — Release

- Merge Release Please PR: `python3 scripts/agent-run.py merge-release-please-pr --wait 300` (auto-merge queue, then `--admin` fallback; requires admin `gh auth`)
- Update @AGENT_MEMORY.md and @DECISION_LOG.md at milestone boundary

## Step 5 — Cleanup

Read @.cursor/commands/cleanup.md — execute fully when sprint or release rows are complete.

Do not force-push, amend published tags, or disable hooks. Halt and escalate [HUMAN] on failure.

Start executing now.
