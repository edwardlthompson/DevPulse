#!/usr/bin/env bash
# Thin wrapper: mechanical autofix + one gate autofix pass (for /prerelease).
# Usage: scripts/prerelease-autofix.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== Pre-release autofix (mechanical) ==="
bash scripts/feature-autofix.sh || true

echo "=== Pre-release autofix (gate loop once) ==="
bash scripts/watch-agent-gates.sh --once --autofix --step none
exit $?
