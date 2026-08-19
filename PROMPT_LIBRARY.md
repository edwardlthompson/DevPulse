# Prompt Library

> Living catalog of highly effective prompt strategies for this repository.

## Entry 1 — Project Initialization (Primary)

**File:** `docs/INITIALIZATION_PROMPT.md`

**Prompt:**

```
Read @docs/START_HERE.md, @docs/CURSOR_MODES.md, and @docs/INITIALIZATION_PROMPT.md.
Pick Cursor mode per CURSOR_MODES.md. Follow Section 8 Startup Sequence.
Use BUILD_PLAN.md Sequential lane first; respect AGENT/HUMAN/ADB/AUTO labels.

```

## Entry 2 — Reference Mode (Existing Project)

**Prompt:**

```
Read @docs/CURSOR_MODES.md, @docs/FOR_AGENTS.md, and @TEMPLATE_INDEX.json.
Pick Cursor mode per CURSOR_MODES.md. Apply matching modules and rules.
Do not scaffold examples/ unless missing.

```

## Entry 3 — Pre-release audit (Agent Mode)

**Prompt:**

```
Run pre-release audit per INITIALIZATION_PROMPT.md Section 7a (Agent Mode).
Verify all quality gates pass. Only then update CHANGELOG.md and create a GitHub Release.

```

## Entry 4 — Build Verification Gate

**Prompt:**

```
Run the Build Verification Gate from INITIALIZATION_PROMPT.md Section 7a.
Execute: check-file-encoding, validate-template-index, validate-bootstrap,
validate-workflow-actions, check-license-compliance, pre-commit run --all-files.
After pushing to main, run scripts/check-github-ci.sh --wait 300
(or scripts/check-github-ci.ps1 -WaitSeconds 300 on Windows).
Report pass/fail per script. Do not mark BUILD_PLAN items complete until all pass.

```

## Entry 5 — Bootstrap Verification

**Prompt:**

```
Run scripts/validate-bootstrap.sh and scripts/validate-template-index.sh.
Confirm .env.example, LICENSE, lockfiles, and TEMPLATE_INDEX.json completeness.
Fix any failures before Sprint 0 sign-off.

```

## Entry 6 — Security Triage

**Prompt:**

```
Follow docs/SECURITY_TRIAGE.md weekly triage pass.
Review Dependabot alerts (Critical/High first), triage open PRs.
Confirm all three required workflows are green on main: CI, Security Scan, CodeQL.
Run scripts/check-github-ci.sh after any workflow or dependency change.

```

## Entry 7 — Pre-Release SBOM Audit

**Prompt:**

```
Before tagging a release, verify release workflow attaches SBOM and provenance.
Review THIRD_PARTY_LICENSES.md and run check-license-compliance.sh after locked installs.

```

## Entry 8 — Workflow Action Validation

**Prompt:**

```
Before committing changes to .github/workflows/, run scripts/validate-workflow-actions.sh.
If gh is unavailable locally, rely on scripts/check-workflow-action-ref-format.sh
(pre-commit) and the CI workflow-actions job. Fix any invalid or bare-semver refs
before push. SHA-pin third-party actions per docs/SECURITY_TRIAGE.md.

```

## Entry 9 — Post-Push GitHub Gate

**Prompt:**

```
After pushing to main, poll required GitHub workflows until green:
  bash scripts/check-github-ci.sh --wait 300
  # Windows: scripts/check-github-ci.ps1 -WaitSeconds 300
Required workflows: CI, Security Scan, CodeQL.
Do not mark release or Sprint 0 complete while any are failing.

```

## Entry 10 — Pre-release gate

**Prompt:**

```
Run scripts/pre-release-gate.sh (or scripts/pre-release-gate.ps1 on Windows).
Confirm CI + Security Scan + CodeQL green, zero Critical/High Dependabot alerts,
.template-version present, then trigger Release workflow_dispatch per reminder output.
Do not tag until the gate passes and CHANGELOG matches .template-version.

```

## Entry 11 — GitHub repo setup

**Prompt:**

```
Run scripts/setup-github-repo.sh (or scripts/setup-github-repo.ps1 on Windows)
with gh authenticated as repo admin. If API returns 422, follow the printed manual
checklist in Settings. Re-run until Dependabot alerts, private vulnerability reporting,
and branch protection (CI, Security Scan, CodeQL) are enabled on main.

```

## Entry 12 - Stack prune complete

**Prompt:**

```
Init stack selection is in .cursor/stack-selection.json.
Read AGENT_MEMORY.md active modules (checkboxes synced by init-project).
Prune examples/ only via init script; never delete LICENSE, CI, or scripts/.
Confirm TEMPLATE_INDEX modules match checked boxes before Parallel lane.

```

## Entry 13 - Session state restore

**Prompt:**

```
Read .cursor-session-state.json if present (schema: .cursor-session-state.example.json).
Restore repo mode, stack, sprint, and sequential_step. Pick Cursor mode via @docs/CURSOR_MODES.md.
Cross-check .cursor/stack-selection.json. Delete session state file after restore.

```

## Entry 14 - Parallel agent dispatch

**Prompt:**

```
Run bash scripts/plan-parallel-dispatch.sh --json for agent_count.
Write .cursor/parallel-scope-lock.json; run check-parallel-scope.sh.
When agent_count >= 2, launch Task subagents in one message (see .cursor/commands/scope.md).
Shared schema stays Sequential; Parallel agents never edit BUILD_PLAN.md.

```

## Entry 15 — Post-release regression

**Prompt:**

```
After merging a Release Please release PR or tagging vX.Y.Z:
1. Run scripts/pre-release-gate.sh and confirm CI + Security Scan + CodeQL green.
2. Verify GitHub Release includes root + per-stack SBOM slices and Winget stub.
3. Confirm GitHub Pages demo deployed (examples/web) with no tracking scripts.
4. Run scripts/simulate-template-upgrade.sh locally or check CI upgrade-simulation job.
5. Append any regressions to KNOWLEDGE_BASE.md and BUILD_PLAN [AUTO] items.

```

## Entry 16 — Template upgrade simulation

**Prompt:**

```
Run scripts/simulate-template-upgrade.sh before marking Sprint M2 complete.
If it fails, follow docs/UPGRADING_FROM_TEMPLATE.md cherry-pick table and fix
validate-bootstrap or validate-template-index gaps before bumping .template-version.
Use action.yml in downstream repos: uses: owner/agent-project-bootstrap/.github/actions/validate-bootstrap@main
(or path: ./ with action.yml at repo root for this template).

```

## Entry 17 — Autonomous feature step

**Prompt:**

```
Autonomous feature step (auto-fix enabled):
  bash scripts/watch-agent-gates.sh --once --autofix
If exit 1: read .cursor/agent-progress.json and gate JSON; fix lint/tests in feature scope; re-run.
Loop with --interval 60 --max-attempts 10 for extended sessions.
On exit 2 (3-strike), halt and escalate to human.
Push to remote still requires human approval.

```

## Entry 18 — Explore / architecture question (Ask Mode)

**Prompt:**

```
Read @docs/CURSOR_MODES.md and @TEMPLATE_INDEX.json.
Explain [topic]. Do not edit files.

```

## Entry 19 — Feature or ADR plan (Plan Mode)

**Prompt:**

```
Read @docs/CURSOR_MODES.md and the active BUILD_PLAN row.
If the trivial rubric says Agent, skip planning and execute directly.
Otherwise pick the single best approach with mandatory resolved ### Critique (Issue→Resolution table) and ### Parallelization before coding.
Bake every critique resolution into the plan body; do not leave open risks or ask the human to address them.
For BUILD_PLAN sprints: maximize agent_count; run check-build-plan-parallel.sh before approval.

```

## Entry 20 — Defect investigation (Debug Mode)

**Prompt:**

```
Read @docs/CURSOR_MODES.md and INITIALIZATION_PROMPT.md Section 7b.
Collect runtime evidence first (command output, CI log URL, repro steps).
Check KNOWLEDGE_BASE.md and docs/FOR_AGENTS.md Failure Playbook.
Confirm repro locally before editing code. Switch to Agent Mode to apply fix.

```

## Entry 21 — Approved BUILD_PLAN execution (Agent Mode)

**Prompt:**

```
Plan approved. Execute the active [AGENT] BUILD_PLAN step in Agent Mode.
After each step: bash scripts/watch-agent-gates.sh --once --autofix
On exit 2 (3-strike), switch to Debug Mode or escalate to human.
Push to remote still requires human approval.

```

## Entry 22 — Full repo audit (`/audit`)

**Slash command:** `.cursor/commands/audit.md`

**Prompt:**

```
Execute the /audit batch command: read @.cursor/commands/audit.md and follow all steps.
Write CODE_REVIEW.md from CODE_REVIEW.md.example; add BUILD_PLAN sprint; execute [AGENT] rows; archive when done.

```

## Entry 23 — Dependabot batch (`/dependabot`)

**Slash command:** `.cursor/commands/dependabot.md` · See also Entry 6.

**Prompt:**

```
Execute @.cursor/commands/dependabot.md — triage alerts/PRs; KB-007 npm overrides when needed.

```

## Entry 24 — Pre-release batch (`/prerelease`)

**Slash command:** `.cursor/commands/prerelease.md` · See also Entries 3, 10, Codex review.

**Prompt:**

```
Execute @.cursor/commands/prerelease.md — prerelease-autofix, optional Codex (/codex-review), hard pre-release-gate; zero Critical/High before /push. Used by /ship.

```

## Entry 24b — Codex review (`/codex-review`)

**Slash command:** `.cursor/commands/codex-review.md` · Docs: `docs/CODEX_REVIEW.md`

**Prompt:**

```
Execute @.cursor/commands/codex-review.md — optional Codex read-only review → CODE_REVIEW.md → BUILD_PLAN Critical/High → /fix. Skip if no OPENAI_API_KEY/CLI.

```

## Entry 25 — Post-release regress (`/regress`)

**Slash command:** `.cursor/commands/regress.md` · See also Entry 15.

**Prompt:**

```
Execute @.cursor/commands/regress.md — SBOM, Pages, upgrade sim after release.

```

## Entry 26 — Release push (`/push`)

**Slash command:** `.cursor/commands/push.md`

**Prompt:**

```
Execute @.cursor/commands/push.md — explicit git push approval granted by user invoking /push.

```

## Entry 27 — Feature slice (`/feature`)

**Slash command:** `.cursor/commands/feature.md` · See also Entry 17.

**Prompt:**

```
Execute @.cursor/commands/feature.md — one BUILD_PLAN feature row; watch-agent-gates after each step.

```

## Entry 28 — Gate autofix (`/fix`)

**Slash command:** `.cursor/commands/fix.md` · See also Entry 17.

**Prompt:**

```
Execute @.cursor/commands/fix.md — watch-agent-gates --once --autofix in active feature scope.

```

## Entry 29 — Sprint 0 init (`/init`)

**Slash command:** `.cursor/commands/init.md` · See also Entry 1.

**Prompt:**

```
Execute @.cursor/commands/init.md — Sprint 0 bootstrap per BUILD_PLAN Child Playbook.

```

## Entry 30 — Stack prune verify (`/prune`)

**Slash command:** `.cursor/commands/prune.md` · See also Entry 12.

**Prompt:**

```
Execute @.cursor/commands/prune.md — verify stack-selection.json and pruned examples.

```

## Entry 31 — CI poll (`/ci`)

**Slash command:** `.cursor/commands/ci.md` · See also Entry 9.

**Prompt:**

```
Execute @.cursor/commands/ci.md — check-github-ci.sh --wait 300 only.

```

## Entry 32 — Docs checks (`/docs`)

**Slash command:** `.cursor/commands/docs.md` · See also Entry 5.

**Prompt:**

```
Execute @.cursor/commands/docs.md — readme health, markdown tables, file encoding.

```

## Entry 33 — Upgrade sim (`/upgrade`)

**Slash command:** `.cursor/commands/upgrade.md` · See also Entry 16.

**Prompt:**

```
Execute @.cursor/commands/upgrade.md — simulate-template-upgrade.sh.

```

## Entry 34 — GitHub setup (`/setup`)

**Slash command:** `.cursor/commands/setup.md` · See also Entry 11.

**Prompt:**

```
Execute @.cursor/commands/setup.md — setup-github-repo.sh; HUMAN on API 422.

```

## Entry 35 — Feature plan (`/plan`)

**Slash command:** `.cursor/commands/plan.md` · See also Entry 19.

**Prompt:**

```
Execute @.cursor/commands/plan.md — single best approach + resolved ### Critique (Issue→Resolution); no code until approved.

```

## Entry 36 — Session restore (`/restore`)

**Slash command:** `.cursor/commands/restore.md` · See also Entry 13.

**Prompt:**

```
Execute @.cursor/commands/restore.md — load .cursor-session-state.json; delete after restore.

```

## Entry 37 — Session compact (`/compact`)

**Slash command:** `.cursor/commands/compact.md` · See also Entry 13.

**Prompt:**

```
Execute @.cursor/commands/compact.md — write .cursor-session-state.json before clearing chat.

```

## Entry 38 — Parallel scope (`/scope`)

**Slash command:** `.cursor/commands/scope.md` · See also Entry 14.

**Prompt:**

```
Execute @.cursor/commands/scope.md — plan-parallel-dispatch.sh manifest + auto Task dispatch.

```

## Entry 39 — Local gates (`/gates`)

**Slash command:** `.cursor/commands/gates.md` · See also Entries 4, 5.

**Prompt:**

```
Execute @.cursor/commands/gates.md — validate-bootstrap --quick, feature-gate, repo hygiene.

```

## Entry 40 — Security triage batch (`/triage`)

**Slash command:** `.cursor/commands/triage.md` · See also Entry 6.

**Prompt:**

```
Execute @.cursor/commands/triage.md — weekly SECURITY_TRIAGE pass.

```

## Entry 41 — Debug batch (`/debug`)

**Slash command:** `.cursor/commands/debug.md` · See also Entry 20.

**Prompt:**

```
Execute @.cursor/commands/debug.md — defect investigation; not the same as /audit.

```

## Entry 42 — Bootstrap super (`/bootstrap`)

**Slash command:** `.cursor/commands/bootstrap.md`

**Prompt:**

```
Execute @.cursor/commands/bootstrap.md — init → prune → setup → gates, then Welcome Tour.

```

## Entry 43 — Verify super (`/verify`)

**Slash command:** `.cursor/commands/verify.md`

**Prompt:**

```
Execute @.cursor/commands/verify.md — docs → gates → ci.

```

## Entry 44 — Build super (`/build`)

**Slash command:** `.cursor/commands/build.md`

**Prompt:**

```
Execute @.cursor/commands/build.md — autonomous BUILD_PLAN sprint chain; no user approval; chains until HUMAN/ADB block or all AGENT/AUTO complete.

```

## Entry 45 — Ship super (`/ship`)

**Slash command:** `.cursor/commands/ship.md`

**Prompt:**

```
Execute @.cursor/commands/ship.md — prerelease → push → regress; grants git push approval.

```

## Entry 46 — Maintain super (`/maintain`)

**Slash command:** `.cursor/commands/maintain.md`

**Prompt:**

```
Execute @.cursor/commands/maintain.md — radar → triage → dependabot → audit.

```

## Entry 47 — Sprint cleanup (`/cleanup`)

**Slash command:** `.cursor/commands/cleanup.md`

**Prompt:**

```
Execute @.cursor/commands/cleanup.md — archive completed BUILD_PLAN rows and sync COMPLETED_TASKS when sprint is fully ✅.

```

## Entry 48 — Coach (`/coach`)

**Slash command:** `.cursor/commands/coach.md`

**Prompt:**

```
Execute @.cursor/commands/coach.md — project health, next BUILD_PLAN action, and industry why.

```
