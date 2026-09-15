#!/usr/bin/env bash
# Require docs/PACKAGE_ATTESTATION.md to cover npm, uv, and GitHub provenance.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/package_attestation_docs.py"
