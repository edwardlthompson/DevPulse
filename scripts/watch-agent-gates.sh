#!/usr/bin/env bash
# Gate loop with mechanical autofix and progress tracking for autonomous agents.
# Usage: watch-agent-gates.sh [--once] [--autofix] [--no-autofix] [--interval SEC]
#   [--max-attempts N] [--wait-ci SEC] [--step LABEL] [--scope auto|full]
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
SCOPE="${FEATURE_GATE_SCOPE:-full}"
SKIP_PREAMBLE_ONCE=false
PIN_SCOPE=false
GATE_MODE="full"
GATE_STACKS=""
GATE_REASON="default"
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
    --scope) SCOPE="${2:-full}"; shift 2 ;;
    --scope=*) SCOPE="${1#*=}"; shift ;;
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

scope_autofix_paths() {
  if [ "$GATE_MODE" = "stacks" ] && [ -n "$GATE_STACKS" ]; then
    echo "$GATE_STACKS" | $PY -c "import sys; print(','.join('examples/'+s.strip() for s in sys.stdin.read().split(',') if s.strip()))"
    return
  fi
  if [ "$GATE_MODE" = "docs" ]; then
    echo "docs,.cursor/commands,BUILD_PLAN.md,CHANGELOG.md"
    return
  fi
  feature_autofix_paths
}

resolve_scope() {
  if [ "$PIN_SCOPE" = true ]; then
    return 0
  fi
  if [ "$SCOPE" != "auto" ]; then
    GATE_MODE="full"
    GATE_STACKS=""
    GATE_REASON="scope-${SCOPE}"
    unset FEATURE_GATE_ONLY || true
    return 0
  fi
  eval "$("$PY" "$ROOT/scripts/lib/gate_scope.py" --shell)"
}

persist_gate_json() {
  printf '%s\n' "$GATE_JSON" | $PY -c "
import json, sys
from pathlib import Path
t = sys.stdin.read()
i = t.rfind('{')
try:
    d = json.loads(t[i:] if i >= 0 else t)
except json.JSONDecodeError:
    d = {'ok': False, 'exit_code': 1, 'failed_stage': None}
Path('.cursor/last-feature-gate.json').write_text(json.dumps(d, indent=2) + chr(10), encoding='utf-8')
"
}

run_gate() {
  local extra=(--json)
  [ -n "$STEP" ] && extra+=(--step "$STEP")
  if [ "$SKIP_PREAMBLE_ONCE" = true ]; then
    extra+=(--skip-preamble)
    SKIP_PREAMBLE_ONCE=false
  fi
  unset FEATURE_GATE_ONLY || true
  case "$GATE_MODE" in
    docs) extra+=(--stack docs) ;;
    stacks)
      case "$GATE_STACKS" in
        *,*) extra+=(--stack multi); export FEATURE_GATE_ONLY="$GATE_STACKS" ;;
        "") extra+=(--stack docs) ;;
        *) extra+=(--stack "$GATE_STACKS") ;;
      esac
      ;;
  esac
  echo "gate scope: mode=$GATE_MODE stacks=${GATE_STACKS:-all} reason=$GATE_REASON"
  set +e
  GATE_JSON="$(bash scripts/feature-gate.sh "${extra[@]}" 2>/dev/null)"
  GATE_EXIT=$?
  set -e
}

attempt=0
while [ "$attempt" -lt "$MAX_ATTEMPTS" ]; do
  attempt=$((attempt + 1))
  echo "watch-agent-gates attempt $attempt/$MAX_ATTEMPTS step=${STEP:-none}"

  resolve_scope
  run_gate
  persist_gate_json
  bash scripts/render-gates-status.sh >/dev/null 2>&1 || true

  if [ "$GATE_EXIT" -eq 0 ]; then
    echo "$GATE_JSON" | $PY -c "import sys,json; t=sys.stdin.read(); i=t.rfind('{'); d=json.loads(t[i:] if i>=0 else t); print('OK', len(d.get('gates_passed',[])), 'stages')" 2>/dev/null || echo "Feature gate passed"
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
    persist_gate_json
    bash scripts/apply-suggested-gate-fixes.sh --json .cursor/last-feature-gate.json || true

    PATHS="$(scope_autofix_paths)"
    if [ -n "$PATHS" ]; then
      bash scripts/feature-autofix.sh --paths "$PATHS" || true
    else
      bash scripts/feature-autofix.sh || true
    fi
    bash scripts/agent-progress.sh record --gate feature-autofix --exit 0 --autofix ${STEP:+--step "$STEP"}

    FAILED="$($PY -c "import json; print(json.load(open('.cursor/last-feature-gate.json',encoding='utf-8')).get('failed_stage') or '')" 2>/dev/null || true)"
    RETRY="$($PY -c "import sys; sys.path.insert(0,'scripts/lib'); from gate_scope import retry_stack; print(retry_stack(sys.argv[1]) or '')" "$FAILED")"
    if [ -n "$RETRY" ]; then
      GATE_MODE=stacks
      GATE_STACKS=$RETRY
      GATE_REASON="retry-$RETRY"
      PIN_SCOPE=true
      SKIP_PREAMBLE_ONCE=true
    elif [ "$FAILED" = "stack-parallel" ] && [ "$GATE_MODE" = "stacks" ]; then
      PIN_SCOPE=true
      SKIP_PREAMBLE_ONCE=true
    fi
    run_gate
    PIN_SCOPE=false
    persist_gate_json
    bash scripts/render-gates-status.sh >/dev/null 2>&1 || true
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
