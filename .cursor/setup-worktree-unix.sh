#!/usr/bin/env bash
# Cursor native worktree setup (fail-soft).
# Allowlist: copy *.env.example only — never .env / credentials.
# Exit 0 on missing stack/tools; exit 1 only if stack-selection.json exists but is corrupt.

set +e
ROOT_SRC="${ROOT_WORKTREE_PATH:-}"
WT="$(pwd)"
echo "worktree setup: cwd=$WT root=${ROOT_SRC:-unset}"

copy_env_examples() {
  local src="$1"
  local dest="$2"
  if [ ! -d "$src" ]; then
    return 0
  fi
  if [ -f "$src/.env.example" ] && [ ! -f "$dest/.env.example" ]; then
    cp "$src/.env.example" "$dest/.env.example" && echo "OK copied .env.example" || echo "SKIP copy .env.example"
  fi
  find "$src" -name '*.env.example' -type f 2>/dev/null | while IFS= read -r f; do
    rel="${f#"$src"/}"
    case "$rel" in
      .env|.env.local|*credentials*) continue ;;
    esac
    mkdir -p "$dest/$(dirname "$rel")"
    if [ ! -f "$dest/$rel" ]; then
      cp "$f" "$dest/$rel" && echo "OK copied $rel" || echo "SKIP copy $rel"
    fi
  done
}

if [ -n "$ROOT_SRC" ] && [ -d "$ROOT_SRC" ]; then
  copy_env_examples "$ROOT_SRC" "$WT"
else
  echo "SKIP env examples (ROOT_WORKTREE_PATH unset or missing)"
fi

STACK_FILE="$WT/.cursor/stack-selection.json"
if [ ! -f "$STACK_FILE" ] && [ -n "$ROOT_SRC" ] && [ -f "$ROOT_SRC/.cursor/stack-selection.json" ]; then
  STACK_FILE="$ROOT_SRC/.cursor/stack-selection.json"
fi

if [ ! -f "$STACK_FILE" ]; then
  echo "SKIP stack install (no stack-selection.json)"
  exit 0
fi

STACK_ERR="$(mktemp)"
STACK="$(python3 -c "
import json, sys
from pathlib import Path
p = Path(sys.argv[1])
try:
    data = json.loads(p.read_text(encoding='utf-8'))
except Exception as e:
    print('CORRUPT:' + str(e), file=sys.stderr)
    sys.exit(2)
print(data.get('stack') or 'multi')
" "$STACK_FILE" 2>"$STACK_ERR")"
RC=$?
if [ "$RC" -eq 2 ]; then
  echo "ERROR corrupt stack-selection.json:" >&2
  cat "$STACK_ERR" >&2
  rm -f "$STACK_ERR"
  exit 1
fi
rm -f "$STACK_ERR"
if [ "$RC" -ne 0 ] || [ -z "$STACK" ]; then
  echo "SKIP stack install (could not read stack)"
  exit 0
fi

echo "stack=$STACK"

has() { command -v "$1" >/dev/null 2>&1; }

install_npm_dir() {
  local dir="$1"
  if [ ! -f "$dir/package.json" ]; then
    echo "SKIP npm in $dir (no package.json)"
    return 0
  fi
  if ! has npm; then
    echo "SKIP npm ci in $dir (npm not on PATH)"
    return 0
  fi
  (cd "$dir" && npm ci) && echo "OK npm ci in $dir" || echo "SKIP npm ci failed in $dir (non-fatal)"
}

install_uv_dir() {
  local dir="$1"
  if [ ! -f "$dir/pyproject.toml" ]; then
    echo "SKIP uv in $dir (no pyproject.toml)"
    return 0
  fi
  if ! has uv; then
    echo "SKIP uv sync in $dir (uv not on PATH)"
    return 0
  fi
  (cd "$dir" && uv sync) && echo "OK uv sync in $dir" || echo "SKIP uv sync failed in $dir (non-fatal)"
}

install_gradle_dir() {
  local dir="$1"
  if [ ! -f "$dir/gradlew" ]; then
    echo "SKIP gradle in $dir (no gradlew)"
    return 0
  fi
  chmod +x "$dir/gradlew" 2>/dev/null || true
  (cd "$dir" && ./gradlew --version) && echo "OK gradle wrapper in $dir" || echo "SKIP gradle failed in $dir (non-fatal)"
}

case "$STACK" in
  web)
    install_npm_dir "$WT/examples/web"
    ;;
  node)
    install_npm_dir "$WT/examples/node"
    ;;
  python)
    install_uv_dir "$WT/examples/python"
    ;;
  android)
    install_gradle_dir "$WT/examples/android"
    ;;
  multi|*)
    install_npm_dir "$WT/examples/web"
    install_npm_dir "$WT/examples/node"
    install_uv_dir "$WT/examples/python"
    install_gradle_dir "$WT/examples/android"
    ;;
esac

echo "worktree setup complete (fail-soft)"
exit 0
