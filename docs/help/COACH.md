# Coach (any IDE)

Next action now — not a backlog. Cursor: `/coach`.

## Paste

```
Read docs/help/COACH.md. One next action + one Why link. Do not implement unless I ask.

```

## Rules

1. Skim `BUILD_PLAN.md` next row + Unreleased (first ~20 CHANGELOG lines). Read `AGENT_MEMORY.md` (Persistent Context + latest retrospective only) and `docs/FIRST_30_DAYS.md` when Week 1 is still open.
2. Run `python3 scripts/agent-run.py project-health` (or `bash scripts/project-health.sh`) and `python3 scripts/agent-run.py feedback-inbox` when available. Summarize stack, repo mode (template vs child), next BUILD_PLAN row, and any dirty Unreleased / unpushed note. If the fix inbox is non-empty, next action is `/audit`. If `ollama=up`, mention `docs/LOCAL_MODELS.md` and `python3 scripts/agent-run.py recommend-model`. In VS Code + Cline, remind the human they can type `/build`, `/tour`, `/setup-local` ([`VSCODE_COMMANDS.md`](VSCODE_COMMANDS.md)). If this is a child repo, run `python3 scripts/agent-run.py check-template-updates`; if it prints a newer template version, offer `/upgrade` (gap plan only).
3. Compare dirty Unreleased vs empty board: open AGENT/AUTO → `/build` (not `/ship`); no AGENT rows + dirty Unreleased → `/ship` or `/prerelease` (not `/ideas`); both empty → `/allideas` or `/ideas` or `/maintain`. If the working tree is large or commits are unpushed, next action is **commit now** then ask the human to `/push` (never silent `git push`; `/split-to-prs` if huge).
4. Reply = **one sentence next action**, then the **industry reason** (link the matching `docs/BEST_PRACTICES.md` subsection). First week → `/tour`. Do not dump whole memory files. Do not update `AGENT_MEMORY.md` unless this is a milestone.
5. Home chrome stays **Settings-only**. Theme and donate live in Settings/About — never in the header.

Word list: [`GLOSSARY.md`](GLOSSARY.md).
