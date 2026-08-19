#!/usr/bin/env bash
# Allowlisted mechanical fixes from feature-gate failed_stage (never free-text).
# Usage: scripts/apply-suggested-gate-fixes.sh [--stage NAME] [--json PATH]
# Reads failed_stage from --json (feature-gate JSON) or --stage.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

STAGE=""
JSON_PATH=""
while [ $# -gt 0 ]; do
  case "$1" in
    --stage) STAGE="${2:-}"; shift 2 ;;
    --stage=*) STAGE="${1#*=}"; shift ;;
    --json) JSON_PATH="${2:-}"; shift 2 ;;
    --json=*) JSON_PATH="${1#*=}"; shift ;;
    *) shift ;;
  esac
done

if [ -z "$STAGE" ] && [ -n "$JSON_PATH" ] && [ -f "$JSON_PATH" ]; then
  STAGE="$($PY -c "import json,sys; print(json.load(open(sys.argv[1],encoding='utf-8')).get('failed_stage') or '')" "$JSON_PATH")"
fi

if [ -z "$STAGE" ]; then
  echo "apply-suggested-gate-fixes: no failed_stage — nothing to do"
  exit 0
fi

echo "apply-suggested-gate-fixes: stage=$STAGE"

case "$STAGE" in
  python-lint|python-format)
    bash scripts/feature-autofix.sh || true
    ;;
  rust-fmt)
    if [ -f examples/rust/Cargo.toml ] && command -v cargo >/dev/null 2>&1; then
      (cd examples/rust && cargo fmt) || true
    fi
    ;;
  go-fmt)
    if [ -f examples/go/go.mod ] && command -v gofmt >/dev/null 2>&1; then
      find examples/go -name '*.go' -print0 | xargs -0 gofmt -w || true
    fi
    ;;
  web-format|web-lint)
    # Only mechanical format — TypeScript errors stay agent-only
    if [ -f examples/web/package.json ] && grep -q '"format"' examples/web/package.json; then
      (cd examples/web && npm run format) || true
    fi
    bash scripts/feature-autofix.sh || true
    ;;
  node-format|node-lint)
    if [ -f examples/node/package.json ] && grep -q '"format"' examples/node/package.json; then
      (cd examples/node && npm run format) || true
    fi
    bash scripts/feature-autofix.sh || true
    ;;
  encoding)
    echo "encoding: no mechanical rewrite — agent must fix UTF-16/BOM"
    ;;
  hygiene|file-limits|*-test|*-type*|*-clippy|*-vet|*-build|design-cohesion|about-feature-gate|android-test)
    echo "stage=$STAGE is semantic — skipping allowlisted shell (agent must fix)"
    ;;
  *)
    echo "stage=$STAGE not in allowlist — skipping"
    ;;
esac

exit 0
