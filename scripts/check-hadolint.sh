#!/usr/bin/env bash
# hadolint --failure-threshold error on .devcontainer/Dockerfile. Skip if missing locally.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/hadolint_check.py"
