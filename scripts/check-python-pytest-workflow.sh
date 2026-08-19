#!/usr/bin/env bash
# Fail when examples/python exists but pytest is missing from deps, config, or CI/gate workflows.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PYPROJECT="$ROOT/examples/python/pyproject.toml"
CI_WORKFLOW="$ROOT/.github/workflows/ci.yml"

if [ ! -f "$PYPROJECT" ]; then
  echo "Python pytest workflow check skipped (no examples/python/pyproject.toml)"
  exit 0
fi

ERRORS=0

fail() {
  echo "FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

if ! grep -qE 'pytest' "$PYPROJECT"; then
  fail "examples/python/pyproject.toml must declare pytest in dependencies"
fi

if ! grep -qE '^\[tool\.pytest\.ini_options\]' "$PYPROJECT"; then
  fail "examples/python/pyproject.toml must define [tool.pytest.ini_options]"
fi

if [ ! -f "$CI_WORKFLOW" ]; then
  fail ".github/workflows/ci.yml missing"
elif ! grep -qE 'uv run pytest' "$CI_WORKFLOW"; then
  fail ".github/workflows/ci.yml must run uv run pytest when Python stack is present"
fi

if ! grep -qE 'python-test.*uv run pytest' "$ROOT/scripts/feature-gate.sh"; then
  fail "scripts/feature-gate.sh must run uv run pytest for the python stack"
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS python pytest workflow check(s) failed"
  exit 1
fi

echo "Python pytest workflow check passed"
