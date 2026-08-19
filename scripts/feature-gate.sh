#!/usr/bin/env bash
# Lint + smoke gate for active stack after feature work.
# Usage: scripts/feature-gate.sh [--json] [--stack web|python|android|node] [--step LABEL]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

JSON=false
STRICT=false
STACK=""
STEP=""
while [ $# -gt 0 ]; do
  case "$1" in
    --json) JSON=true; shift ;;
    --strict) STRICT=true; shift ;;
    --stack=*) STACK="${1#*=}"; shift ;;
    --stack) STACK="${2:-}"; shift 2 ;;
    --step=*) STEP="${1#*=}"; shift ;;
    --step) STEP="${2:-}"; shift 2 ;;
    *) shift ;;
  esac
done

log() {
  if [ "$JSON" = true ]; then
    echo "$@" >&2
  else
    echo "$@"
  fi
}

FAILED_STAGE=""
LOG_TAIL=""
SUGGESTED=()
GATES_PASSED=()

emit_json() {
  local ok="$1" code="$2"
  if [ "$JSON" = true ]; then
    local gp_json
    gp_json="$($PY -c 'import json,sys; print(json.dumps(sys.argv[1:]))' "${GATES_PASSED[@]}")"
    $PY - "$ok" "$code" "$FAILED_STAGE" "$LOG_TAIL" "$STEP" "$gp_json" "${SUGGESTED[@]}" << 'PY'
import json, sys
ok, code = sys.argv[1], int(sys.argv[2])
failed = sys.argv[3] if len(sys.argv) > 3 else ""
log_tail = sys.argv[4] if len(sys.argv) > 4 else ""
step = sys.argv[5] if len(sys.argv) > 5 else ""
gp = json.loads(sys.argv[6]) if len(sys.argv) > 6 and sys.argv[6] else []
fixes = sys.argv[7:] if len(sys.argv) > 7 else []
sys.path.insert(0, "scripts/lib")
from gate_hints import format_human
print(json.dumps({
    "ok": ok == "true",
    "exit_code": code,
    "step": step or None,
    "failed_stage": failed or None,
    "log_tail": log_tail[:2000] if log_tail else None,
    "gates_passed": gp,
    "suggested_fixes": fixes,
    "human_hint": format_human(failed, log_tail) if failed else None,
}, indent=2))
PY
  fi
}

record_progress() {
  local exit_code="$1"
  local gp=""
  if [ "${#GATES_PASSED[@]}" -gt 0 ]; then
    gp="$(IFS=,; echo "${GATES_PASSED[*]}")"
  fi
  local rec=(record --gate feature-gate --exit "$exit_code")
  [ -n "$STEP" ] && rec+=(--step "$STEP" --build-plan-step "$STEP")
  [ -n "$gp" ] && rec+=(--gates-passed "$gp")
  [ -n "$FAILED_STAGE" ] && rec+=(--failed-stage "$FAILED_STAGE")
  [ -n "$LOG_TAIL" ] && rec+=(--log-tail "$LOG_TAIL")
  bash scripts/agent-progress.sh "${rec[@]}" 2>/dev/null || true
}

fail_gate() {
  local stage="$1"
  local log_msg="${2:-}"
  FAILED_STAGE="$stage"
  LOG_TAIL="$log_msg"
  case "$stage" in
    web-lint) SUGGESTED=("fix TypeScript errors in feature scope" "run npm run lint in examples/web" "run npm run format in examples/web if format script exists") ;;
    web-format) SUGGESTED=("run npm run format in examples/web") ;;
    web-test) SUGGESTED=("fix failing vitest in src/{feature}/" "run npm test in examples/web") ;;
    web-build) SUGGESTED=("fix build errors" "run npm run build in examples/web") ;;
    python-lint) SUGGESTED=("run uv run ruff check --fix in examples/python") ;;
    python-format) SUGGESTED=("run uv run ruff format in examples/python") ;;
    python-type) SUGGESTED=("fix mypy/pyright errors in examples/python") ;;
    python-type-mypy) SUGGESTED=("fix mypy errors in examples/python") ;;
    python-type-pyright) SUGGESTED=("fix pyright errors in examples/python") ;;
    python-test) SUGGESTED=("fix pytest failures in examples/python") ;;
    file-limits) SUGGESTED=("split oversized static-data/logic files per AGENTS.md limits") ;;
    android-test) SUGGESTED=("fix JUnit failures" "run ./gradlew test in examples/android") ;;
    design-cohesion) SUGGESTED=("run scripts/check-design-cohesion.sh" "use design tokens and i18n keys") ;;
    about-feature-gate) SUGGESTED=("run scripts/verify-about-feature-gate.sh" "fix About slice regressions") ;;
    rust-fmt) SUGGESTED=("run cargo fmt in examples/rust") ;;
    rust-clippy) SUGGESTED=("fix clippy warnings in examples/rust") ;;
    rust-test) SUGGESTED=("run cargo test in examples/rust") ;;
    go-vet) SUGGESTED=("run go vet in examples/go") ;;
    go-fmt) SUGGESTED=("run gofmt -w in examples/go") ;;
    go-test) SUGGESTED=("run go test in examples/go") ;;
    node-lint) SUGGESTED=("fix lint in examples/node" "run npm run format in examples/node if format script exists") ;;
    node-format) SUGGESTED=("run npm run format in examples/node") ;;
    node-test) SUGGESTED=("fix tests in examples/node") ;;
    *) SUGGESTED=("run scripts/feature-autofix.sh" "fix errors in active feature scope") ;;
  esac
  print_hint "$stage" "$log_msg"
  emit_json false 1
  record_progress 1
  exit 1
}

print_hint() {
  local stage="$1"
  local log_msg="${2:-}"
  local hint
  hint="$("$PY" "$ROOT/scripts/lib/gate_hints.py" "$stage" "$log_msg" 2>/dev/null || true)"
  if [ -z "$hint" ]; then
    return 0
  fi
  if [ "$JSON" = true ]; then
    echo "$hint" >&2
  else
    echo ""
    echo "$hint"
    echo ""
  fi
}

block_env() {
  FAILED_STAGE="environment"
  LOG_TAIL="$1"
  print_hint "environment" "$1"
  emit_json false 2
  record_progress 2
  exit 2
}

if [ -z "$STACK" ] && [ -f .cursor/stack-selection.json ]; then
  STACK="$($PY -c "import json; print(json.load(open('.cursor/stack-selection.json')).get('stack','multi'))" 2>/dev/null || echo multi)"
fi
STACK="${STACK:-multi}"

should_run() {
  local s="$1"
  [ "$STACK" = "multi" ] || [ "$STACK" = "none" ] || [ "$STACK" = "$s" ]
}

skip_or_block() {
  local msg="$1"
  # Single-stack callers use block_env when the required toolchain is missing.
  # For multi/none, missing optional toolchains are always skips — even under --strict
  # (--strict only enables design-cohesion + about-feature-gate for multi).
  log "$msg"
}

run_cmd() {
  local stage="$1"
  shift
  local logfile
  logfile="$(mktemp)"
  if "$@" >"$logfile" 2>&1; then
    GATES_PASSED+=("$stage")
    rm -f "$logfile"
    return 0
  fi
  fail_gate "$stage" "$(tail -n 40 "$logfile")"
}

run_in_dir() {
  local dir="$1"
  shift
  pushd "$dir" >/dev/null
  run_cmd "$@"
  popd >/dev/null
}

log "Feature gate (stack=$STACK step=${STEP:-none} strict=$STRICT)..."

if ! bash scripts/check-repo-hygiene.sh >/dev/null 2>&1; then
  fail_gate "hygiene" "$(bash scripts/check-repo-hygiene.sh 2>&1 | tail -n 20)"
fi
GATES_PASSED+=("hygiene")

bash scripts/sync-exemplar-config.sh >/dev/null 2>&1 || true

if ! bash scripts/check-file-encoding.sh >/dev/null 2>&1; then
  fail_gate "encoding" "$(bash scripts/check-file-encoding.sh 2>&1 | tail -n 20)"
fi
GATES_PASSED+=("encoding")

if ! bash scripts/check-env.sh >/dev/null 2>&1; then
  fail_gate "env" "$(bash scripts/check-env.sh 2>&1 | tail -n 20)"
fi
GATES_PASSED+=("env")

if ! bash scripts/check-file-limits.sh >/dev/null 2>&1; then
  fail_gate "file-limits" "$(bash scripts/check-file-limits.sh 2>&1 | tail -n 20)"
fi
GATES_PASSED+=("file-limits")

if should_run web && [ -f examples/web/package.json ]; then
  if ! command -v npm >/dev/null 2>&1; then
    if [ "$STACK" = "web" ]; then
      block_env "npm not found; install Node.js or set PATH"
    else
      skip_or_block "Skipping web gate (npm not found)"
    fi
  else
    run_in_dir examples/web web-lint npm run lint
    if grep -q '"format:check"' examples/web/package.json 2>/dev/null; then
      run_in_dir examples/web web-format npm run format:check
    fi
    run_in_dir examples/web web-test npm test
    run_in_dir examples/web web-build npm run build
  fi
fi

if should_run python && [ -f examples/python/pyproject.toml ]; then
  if ! command -v uv >/dev/null 2>&1; then
    if [ "$STACK" = "python" ]; then
      block_env "uv not found"
    else
      skip_or_block "Skipping python gate (uv not found)"
    fi
  else
    run_in_dir examples/python python-lint uv run ruff check .
    run_in_dir examples/python python-format uv run ruff format --check .
    run_in_dir examples/python python-type-mypy uv run mypy src
    run_in_dir examples/python python-type-pyright uv run pyright
    run_in_dir examples/python python-test uv run pytest -q
  fi
fi

if should_run android && [ -f examples/android/gradlew ]; then
  if ! command -v java >/dev/null 2>&1 && [ -z "${JAVA_HOME:-}" ]; then
    if [ "$STACK" = "android" ]; then
      block_env "JAVA_HOME not set; Android gate skipped"
    else
      skip_or_block "Skipping android gate (JAVA_HOME not set)"
    fi
  else
    run_in_dir examples/android android-test ./gradlew test --parallel --quiet
  fi
fi

if should_run node && [ -f examples/node/package.json ]; then
  if ! command -v npm >/dev/null 2>&1; then
    if [ "$STACK" = "node" ]; then
      block_env "npm not found"
    else
      skip_or_block "Skipping node gate (npm not found)"
    fi
  else
    run_in_dir examples/node node-lint npm run lint
    if grep -q '"format:check"' examples/node/package.json 2>/dev/null; then
      run_in_dir examples/node node-format npm run format:check
    fi
    run_in_dir examples/node node-test npm test
  fi
fi

if should_run rust && [ -f examples/rust/Cargo.toml ]; then
  if ! command -v cargo >/dev/null 2>&1; then
    if [ "$STACK" = "rust" ]; then
      block_env "cargo not found"
    else
      skip_or_block "Skipping rust gate (cargo not found)"
    fi
  else
    run_in_dir examples/rust rust-fmt cargo fmt --check
    run_in_dir examples/rust rust-clippy cargo clippy -- -D warnings
    run_in_dir examples/rust rust-test cargo test
  fi
fi

if should_run go && [ -f examples/go/go.mod ]; then
  if ! command -v go >/dev/null 2>&1; then
    if [ "$STACK" = "go" ]; then
      block_env "go not found"
    else
      skip_or_block "Skipping go gate (go not found)"
    fi
  else
    run_in_dir examples/go go-vet go vet ./...
    run_in_dir examples/go go-fmt sh -c 'test -z "$(gofmt -l .)"'
    run_in_dir examples/go go-test go test ./...
  fi
fi

if [ "$STRICT" = true ] && [ "$STACK" = "multi" ]; then
  run_cmd design-cohesion bash scripts/check-design-cohesion.sh
  run_cmd about-feature-gate bash scripts/verify-about-feature-gate.sh
fi

log "Feature gate passed (${#GATES_PASSED[@]} stages)."
emit_json true 0
record_progress 0
exit 0
