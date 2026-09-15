#!/usr/bin/env python3
"""sessionStart: one-line stack / distribution context. Fail-open."""
from __future__ import annotations

import json
import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent


def _next_agent_snip() -> str:
    path = ROOT / "BUILD_PLAN.md"
    if not path.is_file():
        return ""
    try:
        in_sync = False
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
            if "🔲" in line and "[AGENT]" in line:
                return stripped[:80]
    except OSError:
        return ""
    return ""


def _open_pr_count() -> str | None:
    """Best-effort open PR count; never block session start long."""
    try:
        sys.path.insert(0, str(ROOT / "scripts" / "lib"))
        from sync_open_prs_build_plan import resolve_gh

        gh = resolve_gh()
        if not gh:
            return None
        proc = subprocess.run(
            [gh, "pr", "list", "--state", "open", "--json", "number", "--limit", "50"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            timeout=2.0,
            check=False,
        )
        if proc.returncode != 0:
            return None
        data = json.loads(proc.stdout or "[]")
        if isinstance(data, list):
            return str(len(data))
    except Exception:
        return None
    return None


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
    parts.append(
        f"local-first cpus={cpus} ram={ram} jobs={jobs} ollama={ollama}: "
        f"prefer This Computer + parallel Task/worktrees/"
        f"/best-of-n over Cloud; BOOTSTRAP_CHECK_JOBS overrides gate parallelism"
    )
    n_prs = _open_pr_count()
    next_agent = _next_agent_snip()
    handoff_bits: list[str] = []
    if n_prs is not None:
        handoff_bits.append(f"open_prs={n_prs}")
    if next_agent:
        handoff_bits.append(f"next_agent={next_agent}")
    handoff_bits.append("type /resume after Cloud")
    parts.append(" ".join(handoff_bits))
    if parts:
        print(json.dumps({"user_message": "Session context: " + ", ".join(parts)}))


if __name__ == "__main__":
    main()
