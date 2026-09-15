#!/usr/bin/env bash
# Validate schemas/golden-path/openvex.example.json (travels with the release SBOM).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/openvex_check.py"
