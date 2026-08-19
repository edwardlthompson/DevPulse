#!/usr/bin/env bash
# Fail when CHANGELOG.md [Unreleased] is missing, duplicated, or not first.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/changelog_unreleased.py" "$ROOT/CHANGELOG.md"
