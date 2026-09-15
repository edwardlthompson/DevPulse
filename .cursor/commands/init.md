# Sprint 0 bootstrap

Guide through `BUILD_PLAN.md` Sprint 0 (from `BUILD_PLAN_TEMPLATE.md` after init).

1. Confirm repo was created via **Use this template** and @docs/INITIALIZATION_PROMPT.md placeholders are filled ([HUMAN] if not).
2. Copy `AGENT.md.example` → `AGENT.md` and paste the original brief **verbatim** (before init). Bootstrap stamps `AGENTS.md` only.
3. Run or verify `scripts/init-project.sh` (or `.ps1`) with chosen stack.
4. Run `scripts/setup-github-repo.sh` when `gh` is authenticated ([HUMAN] on API 422 — follow printed checklist).
5. Run `python3 scripts/agent-run.py validate-bootstrap --quick` and `python3 scripts/agent-run.py feature-gate --stack <active>`.
6. Install local hooks: `pip install pre-commit && pre-commit install && pre-commit run --all-files` (or `python -m pip …` on Windows).
7. Pick Cursor mode per @docs/CURSOR_MODES.md; follow Section 8 Startup Sequence.
8. When Sprint 0 Sequential rows are all ✅ and gates pass, read @.cursor/commands/cleanup.md — execute fully.

Begin now.
