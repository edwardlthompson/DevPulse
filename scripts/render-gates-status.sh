#!/usr/bin/env bash
# Write .cursor/gates-status.md (and a Canvas file when Cursor canvases/ exists).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
exec "$PY" - "$ROOT" "$@" <<'PY'
import sys
from pathlib import Path
root = Path(sys.argv[1])
sys.path.insert(0, str(root / "scripts" / "lib"))
from gates_canvas import fix_banner, write_status
if "--fix-banner" in sys.argv[2:]:
    sys.stdout.write(fix_banner(root))
else:
    path = write_status(root)
    print(f"Wrote {path}")
PY
