#!/usr/bin/env bash
# Check for new upstream template releases on GitHub
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG="$ROOT/.template-update.json"
VERSION_FILE="$ROOT/.template-version"
VERBOSE=false

for arg in "$@"; do
  case "$arg" in
    --verbose) VERBOSE=true ;;
  esac
done

log() {
  if [ "$VERBOSE" = true ]; then
    echo "$@"
  fi
}

if [ ! -f "$CONFIG" ]; then
  echo "No .template-update.json found; skipping update check"
  exit 0
fi

read_config() {
  python3 - "$CONFIG" << 'PY'
import json, sys
with open(sys.argv[1]) as f:
    d = json.load(f)
print(d.get("upstream", "edwardlthompson/agent-project-bootstrap"))
print(d.get("check_interval", "weekly"))
print(d.get("last_checked") or "")
PY
}

IFS=$'\n' read -r UPSTREAM INTERVAL LAST_CHECKED < <(read_config)

VALID_INTERVALS="off daily weekly monthly on_session"
if ! echo "$VALID_INTERVALS" | grep -qw "$INTERVAL"; then
  echo "Warning: invalid check_interval '$INTERVAL'; defaulting to weekly"
  INTERVAL="weekly"
fi

if [ "$INTERVAL" = "off" ]; then
  log "Update check disabled (interval: off)"
  exit 0
fi

should_check() {
  if [ -z "$LAST_CHECKED" ]; then
    return 0
  fi
  python3 - "$LAST_CHECKED" "$INTERVAL" << 'PY'
import sys, datetime
last, interval = sys.argv[1], sys.argv[2]
try:
    dt = datetime.datetime.fromisoformat(last.replace("Z", "+00:00"))
except ValueError:
    sys.exit(0)
now = datetime.datetime.now(datetime.timezone.utc)
deltas = {"daily": 1, "weekly": 7, "monthly": 30, "on_session": 0}
if interval not in deltas and interval != "off":
    days = 7
else:
    days = deltas.get(interval, 7)
if interval == "on_session":
    sys.exit(0)
if (now - dt).days < days:
    sys.exit(1)
PY
}

if ! should_check; then
  log "Skipped: interval throttle ($INTERVAL)"
  exit 0
fi

API_URL="https://api.github.com/repos/${UPSTREAM}/releases/latest"
log "Checking $API_URL ..."

RESPONSE=$(curl -sf "$API_URL" 2>/dev/null || true)
if [ -z "$RESPONSE" ]; then
  echo "Warning: could not reach GitHub API for $UPSTREAM"
  exit 0
fi

LATEST=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tag_name','').lstrip('v'))")
CURRENT=$(cat "$VERSION_FILE" | tr -d '[:space:]')
HTML_URL=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('html_url',''))")

python3 - "$CONFIG" << 'PY'
import json, sys, datetime
with open(sys.argv[1]) as f:
    d = json.load(f)
d["last_checked"] = datetime.datetime.now(datetime.timezone.utc).isoformat()
with open(sys.argv[1], "w") as f:
    json.dump(d, f, indent=2)
    f.write("\n")
PY

version_gt() {
  python3 - "$1" "$2" << 'PY'
import sys
def parse(v):
    return [int(x) for x in v.split(".")]
a, b = parse(sys.argv[1]), parse(sys.argv[2])
sys.exit(0 if a > b else 1)
PY
}

if version_gt "$LATEST" "$CURRENT"; then
  echo "========================================="
  echo " NEW TEMPLATE VERSION AVAILABLE"
  echo " Current:  $CURRENT"
  echo " Latest:   $LATEST"
  echo " Release:  $HTML_URL"
  echo " See docs/UPGRADING_FROM_TEMPLATE.md"
  echo "========================================="
else
  log "Template is up to date ($CURRENT)"
fi
