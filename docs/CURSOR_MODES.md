# Cursor Modes

> Router for Cursor **Ask**, **Plan**, **Agent**, and **Debug** modes. Distinct from Bootstrap/Reference **repo mode** in [`START_HERE.md`](START_HERE.md).

If you are not in Cursor, treat those four names as **roles**: explore / design / implement / diagnose. Your IDE may use different labels. Shared project law is still [`AGENTS.md`](../AGENTS.md) — see [`AGENT_PORTABILITY.md`](AGENT_PORTABILITY.md).

## Mode table

| Mode | When | Artifact | Do not use for |
|------|------|----------|----------------|
| **Ask** | Read-only exploration, architecture questions, index lookup | [`TEMPLATE_INDEX.json`](../TEMPLATE_INDEX.json), [`KNOWLEDGE_BASE.md`](../KNOWLEDGE_BASE.md) | Editing files |
| **Plan** | Non-trivial work: features, ADRs, parallel scope, schema changes | BUILD_PLAN row + resolved `### Critique` (Issue→Resolution) + `### Parallelization` | Mechanical lint fixes |
| **Agent** | Approved plan execution, `[AGENT]` BUILD_PLAN rows, gate autofix | [`watch-agent-gates.sh`](../scripts/watch-agent-gates.sh) | Unapproved architecture |
| **Debug** | Unknown root cause: CI red, flaky tests, 3-strike failures | Runtime logs + KB + [`FOR_AGENTS.md`](FOR_AGENTS.md) Failure Playbook | Pre-release checklists |
Full BUILD_PLAN owner labels (`AGENT`/`HUMAN`/`ADB`/`AUTO`) are orthogonal — see [`BUILD_PLAN.md`](../BUILD_PLAN.md).

## Resolved Critique

Every plan (Cursor Plan Mode, `/plan`, CreatePlan, BUILD_PLAN sprint drafts) must include `### Critique` as an **Issue → Resolution** table. Do this proactively — do not wait for the human to ask about risks.

| Rule | Requirement |
|------|-------------|
| Resolution shape | Verb + artifact (file, test, gate, BUILD_PLAN step, or `[HUMAN]` destructive-ops item) |
| Plan body | Resolutions appear in todos/steps, not critique-only footnotes |
| Decisions | Pick a single best approach; no open questions or unresolved option dumps |
| Deferral | Bare “defer” / “monitor” forbidden unless a tracked follow-up and safety rationale are named |
| Checklist minimum | Null/empty, timeouts, races, unhandled exceptions — each **Resolved** or **N/A** with why |
| When to ask | Only `[HUMAN]` destructive-ops or facts undiscoverable from the codebase |
See [`.cursor/commands/plan.md`](../.cursor/commands/plan.md) and [`.cursor/rules/read-before-write.mdc`](../.cursor/rules/read-before-write.mdc).

## Trivial vs non-trivial

| Signal | Mode | Example in this repo |
|--------|------|----------------------|
| Read-only question | **Ask** | "How does `watch-agent-gates.sh` work?" |
| ≤3 files, no schema/API change, gate autofix | **Agent** | `feature-autofix.sh` pass; KB doc typo |
| New feature container, ADR, parallel scope | **Plan** | Sprint 2 `docs/features/{name}.md` row |
| Same fix failed 3× or CI red, unknown cause | **Debug** | Lighthouse flake (KB-004); Playwright hang (KB-005) |
| Mid-task architecture pivot | **Plan** | Shared type change during feature work |
If uncertain, default to **Plan** and note "uncertain trivial" for human correction.

## When to switch

| From | To | Trigger |
|------|-----|---------|
| Ask | Plan | User says "implement" or "build" |
| Ask | Agent | Trivial fix confirmed by rubric |
| Plan | Agent | Plan approved ("execute the plan"). After Sequential lock, compact or reset `scratchpad.md` (do not replace `AGENT_MEMORY.md`) |
| Agent | Debug | Gate exit 1 after autofix; CI red; flaky repro |
| Agent | Plan | Schema change; scope expanded; file outside feature container |
| Debug | Agent | Root cause confirmed; fix approach agreed |
| Debug | Plan | Fix requires architectural change |
| Any | Ask | Exploratory question mid-session |
Do not debug in Plan Mode. Do not edit in Ask Mode.

```mermaid
flowchart TD
  Start[Session start] --> Pick{Exploring or building?}
  Pick -->|Understand only| Ask[Ask Mode]
  Pick -->|Will change code| Trivial{Non-trivial?}
  Trivial -->|Yes| Plan[Plan Mode]
  Trivial -->|No| Agent[Agent Mode]
  Plan -->|Approved| Agent
  Agent -->|Gate fail or 3-strike| Debug[Debug Mode]
  Debug -->|Root cause found| Agent
  Agent -->|Scope creep| Plan

```

## Prompt shortcuts

| Entry | Mode | See [`PROMPT_LIBRARY.md`](../PROMPT_LIBRARY.md) |
|-------|------|--------------------------------------------------|
| 18 | Ask | Explore / architecture question |
| 19 | Plan | Feature or ADR plan |
| 20 | Debug | Defect investigation |
| 21 | Agent | Approved BUILD_PLAN execution |
| 3 | Agent | Pre-release audit (not Debug) |
Pre-release audit: [`INITIALIZATION_PROMPT.md`](INITIALIZATION_PROMPT.md) §7a. Defect triage: §7b.

## Batch commands

Slash commands in `.cursor/commands/` load recipes when you type `/audit`, `/bootstrap`, etc. **`/audit` ≠ Debug Mode** — audit is a full repo review; use `/debug` or Debug Mode for defect investigation.

| Audience | Doc |
|----------|-----|
| Humans (first time) | [`docs/help/BATCH_COMMANDS.md`](help/BATCH_COMMANDS.md) |
| Agents / maintainers | [`docs/BATCH_COMMANDS.md`](BATCH_COMMANDS.md) |
Bare words (`audit`) also work via `.cursor/rules/batch-commands.mdc`; prefer `/` menu when bare words fail.

## Side chats

Use `/side`, `/btw`, or the Agents Window plus button for a durable side conversation that shares context with the main chat. Default focus is read/search/answer — good for Plan-style exploration and sanity checks **without interrupting** a running Agent. Pull findings back with @-mentions of the side chat.

## Design Mode

Product **Design Mode** (Agents Window browser: click/draw/voice on live UI) applies when the active stack includes **web/PWA**. Do not use it for Android/Python-only work. See [Design Mode docs](https://cursor.com/docs/agent/design-mode).

## Naming disambiguation

| Term | Means | Not the same as |
|------|--------|-----------------|
| **Cloud Agents** | Paid remote VMs (formerly “Background Agents”) | Local Agent Mode |
| **Automations Memories** | Cloud Automations persistence (`MEMORIES.md`-style) | [`AGENT_MEMORY.md`](../AGENT_MEMORY.md) or `.cursor-session-state` |
| Built-in **`/plan`** | Product Plan Mode toggle / CLI plan | Batch [`.cursor/commands/plan.md`](../.cursor/commands/plan.md) orchestrator |
| **`/plan` batch command** | Repo BUILD_PLAN planning recipe | Cursor Plan Mode UI |
CLI mode parity: [`CURSOR_CLI.md`](CURSOR_CLI.md).

## Local compute first

On **This Computer**, prefer machine parallelism over Cloud Agents:

| Lever | Use |
|-------|-----|
| Parallel `/scope` Task subagents | After Sequential lock when `agent_count >= 2` |
| `/worktree` + `/best-of-n` | Isolated local checkouts; multi-model races on hard fixes |
| Side chats | Research in parallel with the main Agent |
| Local gates | `validate-bootstrap` runs independent checks on all CPU cores (`BOOTSTRAP_CHECK_JOBS`) |
Rule: [`.cursor/rules/local-compute.mdc`](../.cursor/rules/local-compute.mdc). Details: [`PARALLEL_AGENT_SCOPES.md`](PARALLEL_AGENT_SCOPES.md), [`CURSOR_INTEGRATIONS.md`](CURSOR_INTEGRATIONS.md).
