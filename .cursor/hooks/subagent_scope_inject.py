#!/usr/bin/env python3
"""subagentStart: inject parallel scope lock when present. Fail-open."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent


def main() -> None:
    lock = ROOT / ".cursor/parallel-scope-lock.json"
    if not lock.is_file():
        return
    try:
        data = json.loads(lock.read_text(encoding="utf-8"))
        agents = data.get("agents") or []
        scopes = [f"{a.get('id', '?')}: {a.get('scope', '')}" for a in agents[:8]]
        if scopes:
            msg = (
                "Parallel scope lock active. Stay inside assigned scope only. "
                + "; ".join(scopes)
            )
            print(json.dumps({"user_message": msg}))
    except Exception:
        pass


if __name__ == "__main__":
    main()
