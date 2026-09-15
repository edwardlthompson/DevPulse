#!/usr/bin/env bash
# Pre-commit helper: mypy the Python Golden Path when it is present.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ ! -f examples/python/pyproject.toml ]; then
  echo "SKIP mypy (no python example)"
  exit 0
fi
if ! command -v uv >/dev/null 2>&1; then
  echo "SKIP mypy (uv not found)"
  exit 0
fi
cd examples/python
uv run mypy src
