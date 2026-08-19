#!/usr/bin/env bash
# Local Codex review → gitignored CODE_REVIEW.md (read-only).
# Exit 0 = wrote review; 3 = skip (no key/CLI); 1 = failure.
# Usage: scripts/run-codex-review.sh [--base REF]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

BASE="origin/main"
while [ $# -gt 0 ]; do
  case "$1" in
    --base) BASE="${2:-origin/main}"; shift 2 ;;
    --base=*) BASE="${1#*=}"; shift ;;
    *) shift ;;
  esac
done

if [ -z "${OPENAI_API_KEY:-}" ]; then
  echo "SKIP: Codex review (no key/CLI)"
  echo "Set OPENAI_API_KEY and install the Codex CLI — see docs/CODEX_REVIEW.md"
  exit 3
fi

if ! command -v codex >/dev/null 2>&1; then
  echo "SKIP: Codex review (no key/CLI)"
  echo "Install Codex CLI — see docs/CODEX_REVIEW.md"
  exit 3
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
OUT_JSON="$TMP_DIR/codex-findings.json"
PROMPT_FILE="$ROOT/.github/codex/prompts/review.md"
SCHEMA_FILE="$ROOT/.github/codex/schemas/findings.json"
HEAD_SHA="$(git rev-parse HEAD 2>/dev/null || echo unknown)"

DIFF_FILE="$TMP_DIR/review.diff"
if git rev-parse --verify "$BASE" >/dev/null 2>&1; then
  git diff "$BASE"...HEAD >"$DIFF_FILE" 2>/dev/null || git diff "$BASE" HEAD >"$DIFF_FILE" || true
else
  git diff HEAD~1...HEAD >"$DIFF_FILE" 2>/dev/null || git diff >"$DIFF_FILE" || true
fi

PROMPT_EXTRA="$TMP_DIR/prompt.md"
{
  cat "$PROMPT_FILE"
  echo ""
  echo "## Context"
  echo ""
  echo "- source: codex-local"
  echo "- head_sha: $HEAD_SHA"
  echo "- base: $BASE"
  echo ""
  echo "## Diff"
  echo ""
  echo '```diff'
  # Cap diff size for token economy
  head -c 120000 "$DIFF_FILE" || true
  echo '```'
} >"$PROMPT_EXTRA"

set +e
# Prefer schema flag when supported; fall back to prompt-only JSON.
if codex exec --help 2>&1 | grep -q -- '--output-schema'; then
  codex exec --sandbox read-only --output-schema "$SCHEMA_FILE" \
    -o "$OUT_JSON" "$(cat "$PROMPT_EXTRA")"
  CODE=$?
else
  codex exec --sandbox read-only \
    "$(cat "$PROMPT_EXTRA")

Write ONLY valid JSON matching the findings schema to stdout." >"$OUT_JSON"
  CODE=$?
fi
set -e

if [ "$CODE" -ne 0 ]; then
  echo "FAIL: codex exec exited $CODE"
  exit 1
fi

if [ ! -s "$OUT_JSON" ]; then
  echo "FAIL: empty Codex output"
  exit 1
fi

# If output is markdown-wrapped JSON, try to extract
$PY - "$OUT_JSON" <<'PY'
import json, re, sys
from pathlib import Path
p = Path(sys.argv[1])
text = p.read_text(encoding="utf-8").strip()
try:
    json.loads(text)
    raise SystemExit(0)
except json.JSONDecodeError:
    pass
m = re.search(r"\{[\s\S]*\}", text)
if not m:
    print("FAIL: could not parse JSON from Codex output", file=sys.stderr)
    raise SystemExit(1)
obj = json.loads(m.group(0))
p.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")
PY

$PY scripts/codex-findings-to-markdown.py \
  --input "$OUT_JSON" \
  --output CODE_REVIEW.md \
  --source codex-local \
  --head-sha "$HEAD_SHA" \
  --allow-empty

echo "OK: wrote CODE_REVIEW.md (head_sha=$HEAD_SHA)"
exit 0
