# Defect investigation (Debug Mode)

Read @docs/CURSOR_MODES.md and @docs/INITIALIZATION_PROMPT.md Section 7b.

**`/debug` ≠ `/audit`.** This is defect triage. Do not start a full-repo review.

## Step 0 — Gate evidence (required)

1. If `.cursor/last-feature-gate.json` exists, read it first. Print `failed_stage`, `exit_code`, `human_hint`, and `log_tail`. Do not start a free-form hunt while this file explains the failure.
2. Read `.cursor/agent-progress.json` `strikes` (default 0 if missing). If `strikes >= 3`: **halt**. Summarize evidence (stage, log tail, fixers already tried). Do not propose a fourth mechanical fix. Escalate to the human.
3. If `last-feature-gate.json` is missing or not JSON: collect runtime evidence (command output, CI log URL, repro steps).

## Step 1 — Playbook

Check @KNOWLEDGE_BASE.md and @docs/FOR_AGENTS.md Failure Playbook.

## Step 2 — Repro, then Agent

Confirm repro locally before editing code. When root cause is identified, switch to Agent Mode (or `/fix`) to apply the fix. `git push` still needs `/push` or `/ship`.

Other IDEs: the same recipe is `docs/help/DEBUG.md`.

Begin now.
