#!/usr/bin/env bash
# Count open Critical/High Dependabot alerts (paginated).
# Usage: scripts/count-critical-high-dependabot.sh
# Exit 0 prints count to stdout; exit 1 on API/auth error.
set -euo pipefail

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI required" >&2
  exit 1
fi

REPO="${GITHUB_REPO:-$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)}"
if [ -z "$REPO" ]; then
  echo "ERROR: gh auth required" >&2
  exit 1
fi

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

COUNT="$("$PY" - "$REPO" << 'PY'
import json, subprocess, sys

repo = sys.argv[1]
proc = subprocess.run(
    [
        "gh",
        "api",
        "--paginate",
        f"repos/{repo}/dependabot/alerts?state=open&per_page=100",
    ],
    capture_output=True,
    text=True,
)
if proc.returncode != 0:
    print(proc.stderr or proc.stdout or "error", file=sys.stderr)
    raise SystemExit(1)

raw = (proc.stdout or "").strip()
if not raw:
    print(0)
    raise SystemExit(0)

# --paginate may concatenate JSON arrays; parse objects incrementally
alerts: list = []
decoder = json.JSONDecoder()
idx = 0
while idx < len(raw):
    while idx < len(raw) and raw[idx].isspace():
        idx += 1
    if idx >= len(raw):
        break
    obj, end = decoder.raw_decode(raw, idx)
    if isinstance(obj, list):
        alerts.extend(obj)
    elif isinstance(obj, dict):
        alerts.append(obj)
    idx = end

total = 0
for a in alerts:
    sev = (a.get("security_vulnerability") or {}).get("severity", "").lower()
    if not sev:
        sev = (a.get("security_advisory") or {}).get("severity", "").lower()
    if sev in ("critical", "high"):
        total += 1
print(total)
PY
)"

echo "$COUNT"
