# Module C: Python Applications

> Activate when your stack includes a Python application or CLI.

## Requirements (Verbatim)

- **Environment & Dependency Locking:** Enforce strict package pinning and environment encapsulation utilizing precise tool-specific lockfiles (e.g., `uv.lock`, `poetry.lock`, or `requirements.txt` with strict hashes).
- **Static Analysis & Type Hygiene:** Enforce complete, strict type-hinting across the codebase validated via type checkers (mypy or pyright). Utilize standard modern tools (such as ruff) to run optimization, styling, and linting suites on every check-in.

## Activation Checklist

- 🔲 Create `pyproject.toml` with strict dependency pins
- 🔲 Generate and commit lockfile (`uv.lock`)
- 🔲 Enable `ruff check` and `ruff format --check` in CI
- 🔲 Enable `mypy --strict` in CI
- 🔲 Enable `pytest` in CI and local feature gate (`uv run pytest -q`)
- 🔲 Review `examples/python/` Golden Path stub
- 🔲 Set coverage budget threshold in CI
- 🔲 Add pre-commit hooks for ruff and mypy
- 🔲 OpenAPI/schema-first design if exposing HTTP API
- 🔲 Contract tests for public API boundaries

## Operations (when deployed as service)

- 🔲 Health/readiness endpoints per `docs/RUNBOOK.md`
- 🔲 Structured logging (JSON, correlation IDs, no PII)

## Golden Path Reference

See `examples/python/` for uv + ruff + mypy + pytest CLI stub.

## Feature gate (Sprint 2+)

After each feature step, `scripts/feature-gate.sh` runs (via `watch-agent-gates.sh`):

| Stage | Command |
|-------|---------|
| Lint | `uv run ruff check .` + `uv run ruff format --check .` |
| Unit | `uv run pytest -q` |

`mypy` remains a milestone gate in full CI; feature gate focuses on fast lint + unit smoke.

## Owner Labels for This Module

| Task type | Label |
|-----------|-------|
| Scaffold package, types, tests | `AGENT` |
| Dependency audit approval | `HUMAN` |
| ruff/mypy/pytest CI gates | `AUTO` |
