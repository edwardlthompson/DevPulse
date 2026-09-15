#!/usr/bin/env bash
# Preview next Release Please version/changelog. Never publishes.
# Usage: scripts/release-please-dry.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"

# Prefer gh's stored credential. A stale 40-char GITHUB_TOKEN in the
# environment makes GraphQL return 401 (release-please) even when `gh` works.
TOKEN=""
if command -v gh >/dev/null 2>&1; then
  TOKEN="$(env -u GITHUB_TOKEN -u GH_TOKEN gh auth token 2>/dev/null || true)"
fi
TOKEN="${TOKEN:-${GITHUB_TOKEN:-${GH_TOKEN:-}}}"
if [ -z "$TOKEN" ]; then
  echo "WARN: no GITHUB_TOKEN/gh auth; skip release-please dry-run (CHANGELOG Unreleased is local source of truth)"
  exit 0
fi

export GITHUB_TOKEN="$TOKEN"
REPO_URL="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
if [ -z "$REPO_URL" ]; then
  echo "WARN: cannot resolve GitHub repo-url; skip release-please dry-run"
  exit 0
fi
export RELEASE_PLEASE_REPO_URL="$REPO_URL"
exec "$PY" - "$ROOT" <<'PY'
import os, shutil, subprocess, sys
from pathlib import Path
root = Path(sys.argv[1])
sys.path.insert(0, str(root / "scripts" / "lib"))
from agent_run_env import child_env
from update_deps import release_please_dry_argv, with_github_token
argv = with_github_token(
    release_please_dry_argv(os.environ["RELEASE_PLEASE_REPO_URL"]),
    os.environ.get("GITHUB_TOKEN", ""),
)
env = child_env()
token = os.environ.get("GITHUB_TOKEN", "")
env["GITHUB_TOKEN"] = token
env.pop("GH_TOKEN", None)
resolved = shutil.which(argv[0], path=env.get("PATH"))
cmd = [resolved, *argv[1:]] if resolved else argv
print("===", " ".join(argv), flush=True)
code = subprocess.call(cmd, cwd=root, env=env)
if code != 0:
    print("WARN: release-please dry-run failed (exit %s); CHANGELOG Unreleased is local source of truth" % code)
    raise SystemExit(0)
raise SystemExit(0)
PY
