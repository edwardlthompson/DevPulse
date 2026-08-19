#!/usr/bin/env bash
# Gate loop with mechanical autofix and progress tracking for autonomous agents.
# Usage: watch-agent-gates.sh [--once] [--autofix] [--no-autofix] [--interval SEC] [--max-attempts N] [--wait-ci SEC] [--step LABEL]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

ONCE=false
AUTOFIX=true
INTERVAL=0
MAX_ATTEMPTS=10
WAIT_CI=0
STEP=""
while [ $# -gt 0 ]; do
  case "$1" in
    --once) ONCE=true; shift ;;
    --autofix) AUTOFIX=true; shift ;;
    --no-autofix) AUTOFIX=false; shift ;;
    --interval) INTERVAL="${2:-60}"; shift 2 ;;
    --max-attempts) MAX_ATTEMPTS="${2:-10}"; shift 2 ;;
    --wait-ci) WAIT_CI="${2:-300}"; shift 2 ;;
    --step) STEP="${2:-}"; shift 2 ;;
    --step=*) STEP="${1#*=}"; shift ;;
    *) shift ;;
  esac
done
STEP="${STEP:-gate}"

if [ "$ONCE" = true ]; then
  MAX_ATTEMPTS=1
  INTERVAL=0
fi

feature_autofix_paths() {
  $PY - "$ROOT" << 'PY'
import json, sys
from pathlib import Path

root = Path(sys.argv[1])
prog = root / ".cursor/agent-progress.json"
feature = ""
stack = "web"
if prog.exists():
    d = json.loads(prog.read_text(encoding="utf-8"))
    feature = d.get("current_feature") or ""
    stack = d.get("stack") or "web"
if not feature:
    print("")
    raise SystemExit(0)

paths = []
if stack in ("web", "multi"):
    paths += [
        f"examples/web/src/{feature}",
        "examples/web/src/components",
        "examples/web/src/main.ts",
    ]
if stack in ("python", "multi"):
    paths += [f"examples/python/src/{feature}"]
if stack in ("android", "multi"):
    paths += [
        f"examples/android/app/src/main/java/dev/foss/goldenpath/{feature}",
        f"examples/android/app/src/main/java/dev/foss/goldenpath/ui/{feature}",
    ]
if stack in ("node", "multi"):
    paths += [f"examples/node/src/{feature}"]
print(",".join(p for p in paths if Path(root / p).exists() or p.endswith("main.ts")))
PY
}

run_gate() {
  local gate_json gate_exit
  GATE_ARGS=(--json)
  [ -n "$STEP" ] && GATE_ARGS+=(--step "$STEP")
  set +e
  gate_json="$(bash scripts/feature-gate.sh "${GATE_ARGS[@]}" 2>/dev/null)"
  gate_exit=$?
  set -e
  GATE_JSON="$gate_json"
  GATE_EXIT="$gate_exit"
}

attempt=0
while [ "$attempt" -lt "$MAX_ATTEMPTS" ]; do
  attempt=$((attempt + 1))
  echo "watch-agent-gates attempt $attempt/$MAX_ATTEMPTS step=${STEP:-none}"

  run_gate

  if [ "$GATE_EXIT" -eq 0 ]; then
    echo "$GATE_JSON" | $PY -c "import sys,json; d=json.load(sys.stdin); print('OK', len(d.get('gates_passed',[])), 'stages')" 2>/dev/null || echo "Feature gate passed"
    if [ "$WAIT_CI" -gt 0 ] && command -v gh >/dev/null 2>&1; then
      echo "Waiting for GitHub CI (${WAIT_CI}s max)..."
      bash scripts/check-github-ci.sh HEAD --wait "$WAIT_CI" || exit 1
    fi
    exit 0
  fi

  echo "$GATE_JSON"

  if [ "$GATE_EXIT" -eq 2 ]; then
    echo "Environment block — halt (exit 2)"
    exit 2
  fi

  STRIKES="$($PY -c "import json; print(json.load(open('.cursor/agent-progress.json')).get('strikes',0))" 2>/dev/null || echo 0)"
  if [ "$STRIKES" -ge 3 ]; then
    echo "3-strike rule: halt (exit 2)"
    exit 2
  fi

  if [ "$AUTOFIX" = true ]; then
    # Allowlisted stage → mechanical command (never free-text suggested_fixes)
    echo "$GATE_JSON" >.cursor/last-feature-gate.json 2>/dev/null || true
    bash scripts/apply-suggested-gate-fixes.sh --json .cursor/last-feature-gate.json || true

    PATHS="$(feature_autofix_paths)"
    if [ -n "$PATHS" ]; then
      bash scripts/feature-autofix.sh --paths "$PATHS" || true
    else
      bash scripts/feature-autofix.sh || true
    fi
    bash scripts/agent-progress.sh record --gate feature-autofix --exit 0 --autofix ${STEP:+--step "$STEP"}
    run_gate
    if [ "$GATE_EXIT" -eq 0 ]; then
      echo "Feature gate passed after autofix"
      exit 0
    fi
    echo "$GATE_JSON"
  fi

  if [ "$ONCE" = true ] || [ "$attempt" -ge "$MAX_ATTEMPTS" ]; then
    echo "Gate failed — agent should apply semantic fixes from JSON and re-run"
    exit 1
  fi

  echo "Sleeping ${INTERVAL}s before retry (agent may fix in parallel)..."
  sleep "$INTERVAL"
done

exit 1
