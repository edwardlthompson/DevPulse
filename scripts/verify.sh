#!/usr/bin/env bash
# Unified local verification harness: env + format/lint/bootstrap gates.
# Usage: scripts/verify.sh [--full]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FULL=false
for arg in "$@"; do
  case "$arg" in
    --full) FULL=true ;;
    --quick) FULL=false ;;
  esac
done

hint_on_fail() {
  local stage="$1"
  shift
  if "$@"; then
    return 0
  fi
  python3 "$ROOT/scripts/lib/gate_hints.py" "$stage" >&2 || true
  return 1
}

echo "=== verify: env schema ==="
hint_on_fail verify-env bash scripts/check-env.sh

echo "=== verify: bootstrap gates ==="
hint_on_fail verify-bootstrap bash scripts/validate-bootstrap.sh --quick

if [ "$FULL" = true ]; then
  STACK="multi"
  if [ -f .cursor/stack-selection.json ]; then
    STACK="$(python3 -c "import json;print(json.load(open('.cursor/stack-selection.json')).get('stack','multi'))" 2>/dev/null || echo multi)"
  fi
  echo "=== verify: feature-gate (stack=$STACK) ==="
  hint_on_fail verify-feature-gate bash scripts/feature-gate.sh --stack "$STACK"
fi

echo "Verification harness passed"
echo "Honesty: verify does not run Android instrumented (connectedDebugAndroidTest)."
echo "  Unit/Robolectric + :app:compileDebugAndroidTestKotlin run via feature-gate --full / watch-agent-gates."
echo "  Device/emulator instrumented needs adb device — use /emulator or ANDROID_SERIAL=… ./gradlew connectedDebugAndroidTest."
echo "Reminder: before a release, run python3 scripts/agent-run.py update-deps (dry-run; do not auto-apply here)."
