"""Generate PROJECT_CHECKLIST.md (Definition of Done after init)."""
from __future__ import annotations

from pathlib import Path

TEMPLATE = """# Project Checklist

> Generated after `scripts/init-project`. Status: 🔲 open · ✅ done · ❌ blocked.
> Project: **{project_name}** · Stack: `{stack}` · License: `{license_id}`

## Setup

- 🔲 README updated with value proposition and quickstart
- 🔲 Environment variables configured (`.env.example` mirrored; `.env` not committed)
- 🔲 `docs/spec.md` and `docs/plan.md` filled for the first milestone
- 🔲 Initial tests passing in the local environment
- 🔲 Pre-commit hooks installed (`pre-commit install`)

## Security & CI (defaults on)

- 🔲 CI workflow verified on GitHub (required check: **CI**)
- 🔲 Security Scan / CodeQL / secret scanning green
- 🔲 Dependabot alerts enabled
- 🔲 Branch protection applied to the default branch
- 🔲 `SECURITY.md` reporting channel confirmed

## Agent adapters

- 🔲 `AGENTS.md` reviewed for this product
- 🔲 Adapters current (`bash scripts/bootstrap-lifecycle.sh --sync-adapters`)
  - `.cursor/rules/main.mdc`
  - `CLAUDE.md`
  - `.github/copilot-instructions.md`

## Next

1. `python3 scripts/agent-run.py validate-bootstrap --quick`
2. `python3 scripts/agent-run.py feature-gate --stack {stack}`
3. `scripts/setup-github-repo.sh` (or `.ps1`) for alerts and branch protection
"""


def write_checklist(
    root: Path,
    *,
    project_name: str,
    stack: str,
    license_id: str,
) -> Path:
    path = root / "PROJECT_CHECKLIST.md"
    text = TEMPLATE.format(
        project_name=project_name or "(unnamed)",
        stack=stack or "none",
        license_id=license_id or "MIT",
    )
    path.write_text(text, encoding="utf-8")
    return path
