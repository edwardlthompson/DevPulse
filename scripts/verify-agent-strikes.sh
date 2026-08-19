#!/usr/bin/env bash
# Regression: 3-strike logic increments on feature-gate failures without --step.
# Usage: scripts/verify-agent-strikes.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
PROGRESS="$ROOT/.cursor/agent-progress.json"
mkdir -p "$ROOT/.cursor"
rm -f "$PROGRESS"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

strikes() {
  "$PY" -c "import json,sys; print(json.load(open(sys.argv[1], encoding='utf-8')).get('strikes',0))" "$PROGRESS" 2>/dev/null || echo 0
}

for _ in 1 2 3; do
  bash scripts/agent-progress.sh record --gate feature-gate --exit 1 >/dev/null
done

s="$(strikes)"
if [ "$s" -lt 3 ]; then
  echo "FAIL: expected strikes>=3 after 3 gate failures, got $s"
  exit 1
fi

bash scripts/agent-progress.sh record --gate feature-autofix --exit 0 --autofix >/dev/null
s="$(strikes)"
if [ "$s" -lt 3 ]; then
  echo "FAIL: autofix reset strikes (got $s)"
  exit 1
fi

bash scripts/agent-progress.sh record --gate feature-gate --exit 0 >/dev/null
s="$(strikes)"
if [ "$s" != "0" ]; then
  echo "FAIL: gate success should reset strikes (got $s)"
  exit 1
fi

echo "verify-agent-strikes passed"
