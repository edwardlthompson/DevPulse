# Local validation gates

> Skills: `.cursor/skills/validate-bootstrap/`, `.cursor/skills/check-repo-hygiene/`, `.cursor/skills/canvas-bootstrap-status/`

Run Sprint 0 / pre-push validation (Git Bash on Windows):

```bash
python3 scripts/agent-run.py validate-bootstrap --quick
python3 scripts/agent-run.py feature-gate --stack multi
python3 scripts/agent-run.py check-repo-hygiene

```

Report pass/fail per script. Fix failures in scope before marking BUILD_PLAN items complete.

**bootstrap-doctor** (alias): same gates — `python3 scripts/agent-run.py validate-bootstrap --quick` (Sprint 0 / local) or `python3 scripts/agent-run.py run-maintainer-gates` (weekly; omit `--quick` for full CI wait). No separate script.

Optional status overview: invoke skill `canvas-bootstrap-status` (Canvas, or markdown table fallback).

Begin now.
