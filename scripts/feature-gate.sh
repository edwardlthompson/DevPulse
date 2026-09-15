#!/usr/bin/env bash
# Lint + smoke gate for active stack after feature work.
# Usage: scripts/feature-gate.sh [--json] [--stack web|python|android|node|rust|go|lightroom|blender|docs|multi] [--step LABEL]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

JSON=false
STRICT=false
STACK=""
STEP=""
SKIP_PREAMBLE=false
while [ $# -gt 0 ]; do
  case "$1" in
    --json) JSON=true; shift ;;
    --strict) STRICT=true; shift ;;
    --skip-preamble) SKIP_PREAMBLE=true; shift ;;
    --stack=*) STACK="${1#*=}"; shift ;;
    --stack) STACK="${2:-}"; shift 2 ;;
    --step=*) STEP="${1#*=}"; shift ;;
    --step) STEP="${2:-}"; shift 2 ;;
    *) shift ;;
  esac
done

if [ -n "${FEATURE_GATE_JOBS:-}" ]; then
  case "$FEATURE_GATE_JOBS" in
    *[!0-9]*|0)
      echo "FAIL: FEATURE_GATE_JOBS must be a positive int" >&2
      exit 2
      ;;
  esac
fi

if [ "${FEATURE_GATE_CHILD:-}" = "1" ]; then
  JSON=false
fi

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
    local gp_json sk_json
    gp_json="$($PY -c 'import json,sys; print(json.dumps(sys.argv[1:]))' "${GATES_PASSED[@]:-}")"
    sk_json="$($PY -c 'import json,sys; print(json.dumps(sys.argv[1:]))' "${SKIPPED_GATES[@]:-}")"
    $PY - "$ok" "$code" "$FAILED_STAGE" "$LOG_TAIL" "$STEP" "$gp_json" "$sk_json" "${SUGGESTED[@]}" << 'PY'
import json, sys
ok, code = sys.argv[1], int(sys.argv[2])
failed = sys.argv[3] if len(sys.argv) > 3 else ""
log_tail = sys.argv[4] if len(sys.argv) > 4 else ""
step = sys.argv[5] if len(sys.argv) > 5 else ""
gp = json.loads(sys.argv[6]) if len(sys.argv) > 6 and sys.argv[6] else []
skipped_raw = json.loads(sys.argv[7]) if len(sys.argv) > 7 and sys.argv[7] else []
fixes = sys.argv[8:] if len(sys.argv) > 8 else []
sys.path.insert(0, "scripts/lib")
from gate_hints import format_human

def skip_entry(msg: str) -> dict:
    reason = msg
    module = None
    low = msg.lower()
    if "rust" in low or "cargo" in low:
        module = "modules/rust/MODULE.md"
        reason = "optional rust stack inactive or cargo missing — see MODULE.md Activation Checklist"
    elif "blender" in low or "cycles" in low:
        module = "modules/blender/MODULE.md"
        reason = "optional blender stack inactive or blender missing — see MODULE.md Activation Checklist"
    return {"message": msg, "reason": reason, "module_md": module}

print(json.dumps({
    "ok": ok == "true",
    "exit_code": code,
    "step": step or None,
    "failed_stage": failed or None,
    "log_tail": log_tail[:2000] if log_tail else None,
    "gates_passed": gp,
    "skipped": [skip_entry(m) for m in skipped_raw if m],
    "suggested_fixes": fixes,
    "human_hint": format_human(failed, log_tail) if failed else None,
}, indent=2))
PY
  fi
}

record_progress() {
  if [ "${FEATURE_GATE_CHILD:-}" = "1" ]; then
    return 0
  fi
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
    web-lighthouse-floors) SUGGESTED=("restore categories:accessibility minScore 0.95 in examples/web/.lighthouserc.json" "keep categories:best-practices minScore at least 0.9" "keep categories:performance minScore at least 0.9" "keep largest-contentful-paint maxNumericValue <= 2500" "keep total-blocking-time maxNumericValue <= 300 (lab INP proxy)") ;;
    web-sw-cache-budget) SUGGESTED=("trim examples/web/public/sw.js PRECACHE list" "keep shell assets under SW_CACHE_MAX_BYTES (default 256KiB)" "run bash scripts/check-sw-cache-budget.sh") ;;
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
    android-fdroid) SUGGESTED=("run scripts/verify-fdroid-metadata.sh") ;;
    android-espresso-16) SUGGESTED=("pin espresso-core:3.7.0 in examples/android/app/build.gradle.kts" "run bash scripts/check-espresso-android16.sh") ;;
    android-compose-a11y) SUGGESTED=("restore examples/android/app/lint.xml a11y issue ids" "keep lint.error ContentDescription in app/build.gradle.kts") ;;
    android-r8) SUGGESTED=("keep isMinifyEnabled = true and proguard-android-optimize.txt" "run bash scripts/check-android-r8.sh") ;;
    android-reproducible-apk) SUGGESTED=("keep SOURCE_DATE_EPOCH=1700000000 on CI android-release" "run bash scripts/check-reproducible-apk.sh") ;;
    android-signing-runbook) SUGGESTED=("keep docs/ANDROID_SIGNING.md env vars and rollback" "run bash scripts/check-android-signing-runbook.sh") ;;
    lightroom-sdk) SUGGESTED=("run scripts/verify-lightroom.sh") ;;
    lightroom-lua-lint) SUGGESTED=("keep Lr* imports only in examples/lightroom" "run bash scripts/check-lightroom-lua.sh") ;;
    lightroom-sdk-playbook) SUGGESTED=("keep Info.lua versions matching examples/lightroom/README.md" "run bash scripts/check-lightroom-sdk-playbook.sh") ;;
    lightroom-tagset-fuzz) SUGGESTED=("keep MetadataTagset.lua reverse-DNS items" "run bash scripts/check-lightroom-tagset-fuzz.sh") ;;
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

if [ "$SKIP_PREAMBLE" = true ] && [ "$STACK" = "multi" ] && [ -z "${FEATURE_GATE_ONLY:-}" ]; then
  echo "FAIL: --skip-preamble requires a single stack (not multi) or FEATURE_GATE_ONLY" >&2
  exit 2
fi

should_run() {
  local s="$1"
  [ "$STACK" = "multi" ] || [ "$STACK" = "none" ] || [ "$STACK" = "$s" ]
}

SKIP_HINT_SHOWN=false
SKIPPED_GATES=()

skip_or_block() {
  local msg="$1"
  # Single-stack callers use block_env when the required toolchain is missing.
  # For multi/none, missing optional toolchains are always skips — even under --strict
  # (--strict only enables design-cohesion + about-feature-gate for multi).
  log "$msg"
  SKIPPED_GATES+=("$msg")
  if [ "$SKIP_HINT_SHOWN" = false ]; then
    SKIP_HINT_SHOWN=true
    log "HINT: install missing tools once (Node 22+npm, uv, JDK17, Android SDK) — see docs/LINUX_DEV.md; agent-run also prepends ~/.local/bin"
    log "HINT: optional Go/Rust MODULE.md skip reasons appear in feature-gate --json skipped[]"
  fi
}

run_cmd() {
  local stage="$1"
  shift
  local logfile secs rc
  logfile="$(mktemp)"
  secs="$("$PY" "$ROOT/scripts/lib/feature_gate_timeout.py" --stage "$stage" 2>/dev/null || echo 180)"
  if command -v timeout >/dev/null 2>&1; then
    set +e
    timeout --signal=TERM "$secs" "$@" >"$logfile" 2>&1
    rc=$?
    set -e
    if [ "$rc" -eq 0 ]; then
      GATES_PASSED+=("$stage")
      rm -f "$logfile"
      return 0
    fi
    if [ "$rc" -eq 124 ]; then
      fail_gate "$stage" "timeout after ${secs}s (FEATURE_GATE_TIMEOUT / FEATURE_GATE_TIMEOUT_${stage%%-*})"
    fi
    fail_gate "$stage" "$(tail -n 40 "$logfile")"
  fi
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

log "Feature gate (stack=$STACK step=${STEP:-none} strict=$STRICT skip_preamble=$SKIP_PREAMBLE)..."

if [ "$SKIP_PREAMBLE" = false ]; then
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

  if ! bash scripts/check-pre-commit-hooks.sh >/dev/null 2>&1; then
    fail_gate "pre-commit-hooks" "$(bash scripts/check-pre-commit-hooks.sh 2>&1 | tail -n 20)"
  fi
  GATES_PASSED+=("pre-commit-hooks")
fi

if [ "$STACK" = "docs" ]; then
  log "Feature gate docs-only (preamble; no stack tests)."
  GATES_PASSED+=("docs-scope")
elif [ "$STACK" = "multi" ]; then
  stack_rc=0
  "$PY" "$ROOT/scripts/lib/run_feature_stacks.py" || stack_rc=$?
  if [ "$stack_rc" -eq 2 ]; then
    block_env "invalid FEATURE_GATE_JOBS"
  fi
  if [ "$stack_rc" -ne 0 ]; then
    fail_gate "stack-parallel" "one or more stack children failed"
  fi
  GATES_PASSED+=("stack-parallel")
else
if should_run web && [ -f examples/web/package.json ]; then
  if ! command -v npm >/dev/null 2>&1; then
    if [ "$STACK" = "web" ] && [ "${FEATURE_GATE_CHILD:-}" != "1" ]; then
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
  run_cmd locale-pack-budget bash scripts/check-locale-pack-budget.sh
    run_in_dir examples/web web-build npm run build
    run_cmd web-lighthouse-floors bash scripts/check-lighthouse-floors.sh
    run_cmd web-sw-cache-budget bash scripts/check-sw-cache-budget.sh
  fi
fi

if should_run python && [ -f examples/python/pyproject.toml ]; then
  if ! command -v uv >/dev/null 2>&1; then
    if [ "$STACK" = "python" ] && [ "${FEATURE_GATE_CHILD:-}" != "1" ]; then
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

android_sdk_ready() {
  if [ -n "${ANDROID_HOME:-}" ] && [ -d "${ANDROID_HOME}" ]; then
    return 0
  fi
  if [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -d "${ANDROID_SDK_ROOT}" ]; then
    return 0
  fi
  if [ -f examples/android/local.properties ] && grep -q '^sdk.dir=' examples/android/local.properties; then
    return 0
  fi
  return 1
}

if should_run android && [ -f examples/android/gradlew ]; then
  if ! command -v java >/dev/null 2>&1 && [ -z "${JAVA_HOME:-}" ]; then
    if [ "$STACK" = "android" ] && [ "${FEATURE_GATE_CHILD:-}" != "1" ]; then
      block_env "JAVA_HOME not set; Android gate skipped"
    else
      skip_or_block "Skipping android gate (JAVA_HOME not set)"
    fi
  elif ! android_sdk_ready; then
    skip_or_block "Skipping android gate (no ANDROID_HOME / sdk.dir)"
  else
    gradle_extra="$("$PY" "$ROOT/scripts/lib/gradle_offline.py" --args --root "$ROOT" 2>/dev/null || true)"
    # shellcheck disable=SC2086
    run_in_dir examples/android android-test ./gradlew $gradle_extra test --parallel --quiet
    # Compile instrumented tests without starting an emulator.
    # shellcheck disable=SC2086
    run_in_dir examples/android android-compile-androidtest \
      ./gradlew $gradle_extra :app:compileDebugAndroidTestKotlin --parallel --quiet
  fi
fi

if should_run android && [ -f examples/android/app/build.gradle.kts ]; then
  run_cmd android-espresso-16 bash scripts/check-espresso-android16.sh
fi

if should_run android && [ -d examples/android/metadata ]; then
  run_cmd android-fdroid bash scripts/verify-fdroid-metadata.sh
fi

if should_run android && [ -f examples/android/app/lint.xml ]; then
  run_cmd android-compose-a11y bash scripts/check-compose-a11y-lint.sh
fi

if should_run android && [ -f schemas/golden-path/app-version.json ]; then
  run_cmd android-app-version bash scripts/check-golden-path-app-version.sh
fi

if should_run android && [ -f examples/android/app/proguard-rules.pro ]; then
  run_cmd android-r8 bash scripts/check-android-r8.sh
fi

if should_run android && [ -f scripts/verify-reproducible-apk.sh ]; then
  run_cmd android-reproducible-apk bash scripts/check-reproducible-apk.sh
fi

if should_run android && [ -f scripts/check-android-signing-runbook.sh ]; then
  run_cmd android-signing-runbook bash scripts/check-android-signing-runbook.sh
fi

if should_run node && [ -f examples/node/package.json ]; then
  if ! command -v npm >/dev/null 2>&1; then
    if [ "$STACK" = "node" ] && [ "${FEATURE_GATE_CHILD:-}" != "1" ]; then
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
    if [ "$STACK" = "rust" ] && [ "${FEATURE_GATE_CHILD:-}" != "1" ]; then
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
    if [ "$STACK" = "go" ] && [ "${FEATURE_GATE_CHILD:-}" != "1" ]; then
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

if should_run lightroom && [ -f examples/lightroom/Info.lua ]; then
  run_cmd lightroom-sdk bash scripts/verify-lightroom.sh
  run_cmd lightroom-lua-lint bash scripts/check-lightroom-lua.sh
  run_cmd lightroom-sdk-playbook bash scripts/check-lightroom-sdk-playbook.sh
  run_cmd lightroom-tagset-fuzz bash scripts/check-lightroom-tagset-fuzz.sh
fi

if should_run blender && [ -f examples/blender/blender.toml ]; then
  run_cmd blender-stub "$PY" examples/blender/cli.py --stub --limit 1
  run_cmd blender-qa "$PY" -m unittest tests.test_blender_icon_factory
  if command -v blender >/dev/null 2>&1; then
    run_cmd blender-cpu blender --background --python examples/blender/cli.py -- --limit 1
  else
    skip_or_block "Skipping blender Cycles (blender not found)"
  fi
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
