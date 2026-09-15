"""Optional /gates canvas health one-liners (CI red + dirty tree)."""

from __future__ import annotations

import subprocess
from pathlib import Path


def dirty_tree_one_liner(root: Path) -> str:
    """Short git dirty summary for canvas status (never runs network)."""
    try:
        proc = subprocess.run(
            ["git", "-C", str(root), "status", "--porcelain"],
            capture_output=True,
            text=True,
            timeout=10,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return "Dirty tree: unknown (git unavailable)"
    if proc.returncode != 0:
        return "Dirty tree: unknown (git error)"
    lines = [ln for ln in proc.stdout.splitlines() if ln.strip()]
    if not lines:
        return "Dirty tree: clean"
    return f"Dirty tree: {len(lines)} path(s) changed"


def ci_status_one_liner(root: Path) -> str:
    """Reuse health_ci red one-liner when importable; otherwise a soft skip."""
    try:
        from health_ci import ci_red_one_liner  # type: ignore

        return ci_red_one_liner(root)
    except Exception:
        return "CI: unknown (health_ci unavailable)"


def fix_banner(root: Path) -> str:
    import json

    def _json(path: Path) -> dict:
        if not path.is_file():
            return {}
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            return {}
        return data if isinstance(data, dict) else {}

    strikes = _json(root / ".cursor" / "agent-progress.json").get("strikes", 0)
    try:
        strikes_n = int(strikes)
    except (TypeError, ValueError):
        strikes_n = 0
    stage = _json(root / ".cursor" / "last-feature-gate.json").get("failed_stage") or "none"
    return f"strikes={strikes_n}\nfailed_stage={stage}\n"
