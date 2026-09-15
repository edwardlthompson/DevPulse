#!/usr/bin/env bash
# Local-first dependency updater. Dry-run by default. Never git push.
# Usage: scripts/update-deps.sh [--dry-run|--apply|--audit]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/update_deps_cli.py" "$@"
