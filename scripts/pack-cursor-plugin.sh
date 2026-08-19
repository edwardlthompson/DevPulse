#!/usr/bin/env bash
# Pack FOSS Cursor components into dist/cursor-plugin/ (standard plugin layout).
# Idempotent. Does not modify live .cursor/. Output is gitignored.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/dist/cursor-plugin"
SRC="$ROOT/.cursor"

rm -rf "$OUT"
mkdir -p "$OUT/.cursor-plugin" "$OUT/rules" "$OUT/skills" "$OUT/agents" "$OUT/commands" "$OUT/hooks"

cp "$ROOT/.cursor-plugin/plugin.json" "$OUT/.cursor-plugin/plugin.json"

# Wholesale FOSS dirs (new skills/rules included automatically)
if [ -d "$SRC/rules" ]; then
  # Skip commercial-only rule content if present as inactive — copy all .mdc; sync toggles alwaysApply
  cp -R "$SRC/rules/." "$OUT/rules/"
fi
if [ -d "$SRC/skills" ]; then
  cp -R "$SRC/skills/." "$OUT/skills/"
fi
if [ -d "$SRC/agents" ]; then
  cp -R "$SRC/agents/." "$OUT/agents/"
fi
if [ -d "$SRC/commands" ]; then
  cp -R "$SRC/commands/." "$OUT/commands/"
fi

cp "$SRC/hooks.json" "$OUT/hooks.json"
if [ -d "$SRC/hooks" ]; then
  # Python hooks + denylist only (no commercial cloud example)
  find "$SRC/hooks" -maxdepth 1 \( -name '*.py' -o -name 'shell-denylist.txt' \) -exec cp {} "$OUT/hooks/" \;
fi

# Optional FOSS helpers (not required by plugin schema)
cp "$SRC/permissions.json" "$OUT/permissions.json" 2>/dev/null || true
cp "$SRC/worktrees.json" "$OUT/worktrees.json" 2>/dev/null || true
cp "$SRC/setup-worktree-unix.sh" "$OUT/setup-worktree-unix.sh" 2>/dev/null || true
cp "$SRC/setup-worktree-windows.ps1" "$OUT/setup-worktree-windows.ps1" 2>/dev/null || true

# Explicitly exclude commercial / secrets
rm -f "$OUT"/mcp.json "$OUT"/.env 2>/dev/null || true
find "$OUT" -name '*.commercial.example' -delete 2>/dev/null || true
find "$OUT" -name 'BUGBOT.md' -delete 2>/dev/null || true
find "$OUT" -name 'environment.json' -delete 2>/dev/null || true

echo "OK packed FOSS plugin → $OUT"
echo "Local test: symlink this directory to ~/.cursor/plugins/local/agent-project-bootstrap (not the repo root)"
