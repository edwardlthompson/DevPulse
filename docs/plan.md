# Implementation Plan

> Task breakdown stub. Child repos replace this with the first milestone. Active work lives in `BUILD_PLAN.md`.
> Status: 🔲 open · ✅ done · ❌ blocked.

## Milestone — Bootstrap standards (M36)

| Task | Owner | Tests / fallback |
|------|-------|------------------|
| ✅ Manifest + pre/post hooks | AGENT | `tests/test_bootstrap_engine.py` |
| ✅ AGENTS.md spec + adapters | AGENT | `check-bootstrap-engine.sh` + file presence |
| ✅ SDD stubs (`docs/spec.md`, this file) | AGENT | `validate-bootstrap.sh` REQUIRED list |
| ✅ Init dry-run asserts new artifacts | AGENT | `simulate-template-upgrade.sh` |

## Next feature

1. Copy `docs/features/_template.md` → `docs/features/{name}.md`
2. Lock the public API (Sequential)
3. Add unit tests before or with the implementation
4. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

If automated tests are not feasible, write the justification and fallback command in the feature spec before marking the BUILD_PLAN row ✅.
