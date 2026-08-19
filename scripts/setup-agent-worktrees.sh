#!/usr/bin/env bash
# Create git worktrees for parallel agent branches (optional hard isolation).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MANIFEST="${1:-$ROOT/.cursor/parallel-scope-lock.json}"
if [ ! -f "$MANIFEST" ]; then
  echo "Missing $MANIFEST — run plan-parallel-dispatch.sh and write scope lock first" >&2
  exit 1
fi
python3 - "$ROOT" "$MANIFEST" << 'PY'
import json, subprocess, sys
from pathlib import Path

root = Path(sys.argv[1])
manifest = json.loads(Path(sys.argv[2]).read_text(encoding="utf-8"))
worktrees = root / ".cursor" / "worktrees"
worktrees.mkdir(parents=True, exist_ok=True)
for agent in manifest.get("agents", []):
    branch = agent.get("branch", "")
    slug = branch.replace("feature/agent-", "") or agent.get("id", "x").lower()
    path = worktrees / slug
    if path.exists():
        print(f"SKIP {path} (exists)")
        continue
    subprocess.run(
        ["git", "worktree", "add", "-b", branch, str(path), "HEAD"],
        cwd=root,
        check=True,
    )
    print(f"OK worktree {path} branch {branch}")
PY
