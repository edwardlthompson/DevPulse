#!/usr/bin/env bash
# Pre/post bootstrap hooks: preflight, adapters, checklist, manifest.
# Usage: scripts/bootstrap-lifecycle.sh [--pre|--post|--sync-adapters|--checklist|--all] [options]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
source "$ROOT/scripts/lib/resolve-python.sh"

export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" "$ROOT/scripts/lib/bootstrap_cli.py" --root "$ROOT" "$@"
