#!/usr/bin/env python3
"""beforeMCPExecution: audit log + FOSS server allowlist. Fail-open on parse errors."""
from __future__ import annotations

import json
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from mcp_allowlist import decide  # noqa: E402


def main() -> None:
    try:
        data = json.load(sys.stdin)
    except Exception:
        print(json.dumps({"permission": "allow"}))
        return

    log = ROOT / ".cursor/mcp-audit.log"
    log.parent.mkdir(parents=True, exist_ok=True)
    ts = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    tool = data.get("tool_name") or data.get("name") or "unknown"
    server = data.get("server") or data.get("mcp_server") or "unknown"
    line = f"{ts} server={server} tool={tool}\n"
    try:
        with log.open("a", encoding="utf-8") as fh:
            fh.write(line)
    except OSError:
        pass
    print(json.dumps(decide(data, ROOT)))


if __name__ == "__main__":
    main()
