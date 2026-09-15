#!/usr/bin/env bash
# Property / fuzz check for examples/lightroom MetadataTagset factory.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=/dev/null
. "$ROOT/scripts/lib/resolve-python.sh"
cd "$ROOT"
exec "$PY" "$ROOT/scripts/lib/lightroom_tagset_fuzz.py"
