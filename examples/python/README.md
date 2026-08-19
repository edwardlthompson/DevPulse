<p align="center">
  <img src="../../branding/assets/logo-mark.svg" alt="Golden Path" width="64" />
</p>

# Golden Path Python

FOSS apps with a clear path from idea to release — uv + ruff + mypy + pytest CLI stub. Brand kit: [`branding/BRANDING.md`](../../branding/BRANDING.md).

## Commands

```bash
uv sync --all-extras
uv run pytest
# `-n auto` (pytest-xdist) uses local CPU cores; override with e.g. uv run pytest -n 2
uv run ruff check .
uv run ruff format --check .
uv run mypy src
uv run hello FOSS

```

Optional task runner (not required for CI): install [just](https://github.com/casey/just), then `just qa`.

## Why these tools?

uv locks installs, ruff is fast lint/format, mypy catches type bugs before pytest, and xdist uses every core so the 90% coverage gate stays cheap. That is the same toolchain most 2025 Python FOSS packages ship.

## Features

- Strict type hints validated by mypy
- ruff lint and format checks
- pytest with 90% coverage budget and pytest-xdist (`-n auto`)
- Pure business logic in `greet.py`, CLI in `cli.py`

## CI Integration

Runs in root `.github/workflows/ci.yml` Python job (`uv run pytest`) and weekly health check pytest smoke when `examples/python/` is present.
