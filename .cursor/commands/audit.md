# Full repo review and BUILD_PLAN execution

> Skill: `.cursor/skills/check-repo-hygiene/`

Framework: use AGENT/HUMAN/ADB/AUTO labels; Sequential before Parallel; gates after AGENT steps; update memory files at milestones.

## Step 1 — Review

Explore via targeted reads (active stack only — @docs/FOR_AGENTS.md token economy). Run when available:

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py feature-gate --stack multi
python3 scripts/agent-run.py check-repo-hygiene
python3 scripts/agent-run.py check-readme-health
```


Check Dependabot/CodeQL via `gh` if authenticated. Write @CODE_REVIEW.md from @CODE_REVIEW.md.example (severity: Critical / High / Medium / Low / Deferred).

## Step 2 — BUILD_PLAN

Add a review sprint at the top of @BUILD_PLAN.md active board. Link findings to CODE_REVIEW sections. Use 🔲 [AGENT] / 🔲 [HUMAN] format (✅ done · ❌ blocked per BUILD_PLAN legend).

## Step 3 — Execute

Work Sequential [AGENT] items top-to-bottom. After each step:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --step none
```

## Step 4 — Cleanup

Read @.cursor/commands/cleanup.md — execute fully.

Begin now.
