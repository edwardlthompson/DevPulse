#!/usr/bin/env python3
"""sessionStart: one-line stack / next-agent context. Fail-open."""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent


def _next_agent_snip() -> str:
    path = ROOT / "BUILD_PLAN.md"
    if not path.is_file():
        return ""
    try:
        in_sync = False
        local_hit = ""
        any_agent = ""
        cloud_open = False
        for line in path.read_text(encoding="utf-8").splitlines():
            if "<!-- open-prs-sync:begin -->" in line:
                in_sync = True
                continue
            if "<!-- open-prs-sync:end -->" in line:
                in_sync = False
                continue
            if in_sync:
                continue
            stripped = line.strip()
            if not (stripped[:1].isdigit() or stripped.startswith("-")):
                continue
            if "🔲" not in line or "[AGENT]" not in line:
                continue
            if "[AGENT][CLOUD]" in line:
                cloud_open = True
                continue
            if "[AGENT][LOCAL]" in line and not local_hit:
                local_hit = stripped[:80]
            elif not any_agent:
                any_agent = stripped[:80]
        if local_hit:
            return local_hit + (" (cloud_rows_open)" if cloud_open else "")
        if cloud_open and not any_agent:
            return "cloud_rows_open type /resume"
        return any_agent
    except OSError:
        return ""
    return ""


def main() -> None:
    parts: list[str] = []
    sel = ROOT / ".cursor/stack-selection.json"
    if sel.is_file():
        try:
            data = json.loads(sel.read_text(encoding="utf-8"))
            stack = data.get("stack", "?")
            tier = data.get("distribution_tier", "foss")
            parts.append(f"stack={stack} tier={tier}")
        except json.JSONDecodeError:
            pass
    cpus = os.cpu_count() or 1
    ram = "?"
    jobs = "?"
    ollama = "down"
    try:
        sys.path.insert(0, str(ROOT / "scripts" / "lib"))
        from local_resources import ollama_up, ram_gb_or_none, recommended_check_jobs

        gb = ram_gb_or_none()
        ram = str(gb) if gb is not None else "?"
        jobs = str(recommended_check_jobs())
        ollama = "up" if ollama_up() else "down"
    except Exception:
        pass
    parts.append(f"local-first cpus={cpus} ram={ram} jobs={jobs} ollama={ollama}")
    next_agent = _next_agent_snip()
    bits = [f"next_agent={next_agent}"] if next_agent else []
    bits.append("type /resume after Cloud")
    parts.append(" ".join(bits))
    if parts:
        print(json.dumps({"user_message": "Session context: " + ", ".join(parts)}))


if __name__ == "__main__":
    main()
