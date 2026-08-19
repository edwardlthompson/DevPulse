#!/usr/bin/env bash
# Validate .env.example against env.schema.json (and .env if present).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
source "$ROOT/scripts/lib/resolve-python.sh"

export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" -c "
from pathlib import Path
import sys
from env_schema import validate_env
errors = validate_env(Path('.').resolve())
if errors:
    for e in errors:
        print(f'ERROR: {e}', file=sys.stderr)
    sys.exit(1)
print('Env schema check passed')
"
