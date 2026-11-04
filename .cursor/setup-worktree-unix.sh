#!/usr/bin/env bash
# Cursor native worktree setup (fail-soft).
# Allowlist: copy *.env.example only — never .env / credentials.
# Exit 0 on missing stack/tools; exit 1 only if stack-selection.json exists but is corrupt.

set +e
ROOT_SRC="${ROOT_WORKTREE_PATH:-}"
WT="$(pwd)"
echo "worktree setup: cwd=$WT root=${ROOT_SRC:-unset}"

RESOLVE=""
for d in "$WT" "$ROOT_SRC"; do
  if [ -n "$d" ] && [ -f "$d/scripts/lib/resolve-python.sh" ]; then
    RESOLVE="$d/scripts/lib/resolve-python.sh"
    break
  fi
done

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

RESOLVE=""
for d in "$WT" "$ROOT_SRC"; do
  if [ -n "$d" ] && [ -f "$d/scripts/lib/resolve-python.sh" ]; then
    RESOLVE="$d/scripts/lib/resolve-python.sh"
    break
  fi
done
if [ -n "$RESOLVE" ]; then
  # shellcheck source=/dev/null
  . "$RESOLVE"
else
  PY="${PY:-python3}"
fi

export ROOT_WORKTREE_PATH="${ROOT_SRC}"
if ! "$PY" -c "
from pathlib import Path
import os, sys
root = os.environ.get('ROOT_WORKTREE_PATH') or ''
try:
    ok = bool(root) and Path(root).is_dir() and Path(root).resolve() != Path('.').resolve()
except OSError:
    ok = False
raise SystemExit(0 if ok else 1)
"; then
  echo "SKIP stack install (not a Cursor worktree; set ROOT_WORKTREE_PATH to the primary repo)"
  exit 0
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
STACK="$( "$PY" -c "
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

link_platform_tools() {
  local sdk=""
  local dest="$WT/.cursor/platform-tools"
  for key in ANDROID_HOME ANDROID_SDK_ROOT; do
    eval "val=\${$key:-}"
    if [ -n "$val" ] && [ -d "$val/platform-tools" ]; then
      sdk="$val/platform-tools"
      break
    fi
  done
  if [ -z "$sdk" ]; then
    for candidate in "$HOME/Android/Sdk/platform-tools" "$HOME/.local/android/platform-tools"; do
      if [ -d "$candidate" ]; then
        sdk="$candidate"
        break
      fi
    done
  fi
  if [ -z "$sdk" ]; then
    echo "SKIP platform-tools symlink (SDK not found)"
    return 0
  fi
  mkdir -p "$WT/.cursor"
  if [ -L "$dest" ] || [ -d "$dest" ]; then
    echo "OK platform-tools already present at $dest"
    return 0
  fi
  ln -s "$sdk" "$dest" && echo "OK linked platform-tools -> $sdk" || echo "SKIP platform-tools symlink (non-fatal)"
}

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
  (cd "$dir" && npm ci --prefer-offline --no-audit --no-fund) && echo "OK npm ci in $dir" || {
    echo "WARN npm ci --prefer-offline failed in $dir; retry without offline"
    (cd "$dir" && npm ci --no-audit --no-fund) && echo "OK npm ci in $dir" || echo "SKIP npm ci failed in $dir (non-fatal)"
  }
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

gradle_offline_py() {
  for d in "$WT" "$ROOT_SRC"; do
    if [ -n "$d" ] && [ -f "$d/scripts/lib/gradle_offline.py" ]; then
      echo "$d/scripts/lib/gradle_offline.py"
      return 0
    fi
  done
  return 1
}

install_gradle_dir() {
  local dir="$1"
  local helper extra
  if [ ! -f "$dir/gradlew" ]; then
    echo "SKIP gradle in $dir (no gradlew)"
    return 0
  fi
  chmod +x "$dir/gradlew" 2>/dev/null || true
  helper="$(gradle_offline_py || true)"
  extra=""
  if [ -n "$helper" ] && [ -n "$ROOT_SRC" ]; then
    extra="$("$PY" "$helper" --args --root "$ROOT_SRC" 2>/dev/null || true)"
  fi
  if [ -n "$extra" ]; then
    if (cd "$dir" && ./gradlew --offline --version); then
      echo "OK gradle wrapper in $dir (offline)"
      return 0
    fi
    echo "WARN gradle --offline failed in $dir; retry online"
  fi
  if (cd "$dir" && ./gradlew --version); then
    if [ -n "$helper" ] && [ -n "$ROOT_SRC" ]; then
      "$PY" "$helper" --mark --root "$ROOT_SRC" 2>/dev/null || true
    fi
    echo "OK gradle wrapper in $dir"
  else
    echo "SKIP gradle failed in $dir (non-fatal)"
  fi
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
    link_platform_tools
    install_gradle_dir "$WT/examples/android"
    ;;
  multi|*)
    install_npm_dir "$WT/examples/web"
    install_npm_dir "$WT/examples/node"
    install_uv_dir "$WT/examples/python"
    link_platform_tools
    install_gradle_dir "$WT/examples/android"
    ;;
esac

echo "worktree setup complete (fail-soft)"
exit 0
