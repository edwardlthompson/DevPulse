"""Decide whether Release Please merge should skip the auto-merge wait."""
from __future__ import annotations


def skip_auto_merge_wait(rollup: list[dict] | None, merge_state: str = "") -> bool:
    """True when GitHub will never turn the PR green (empty or ACTION_REQUIRED)."""
    state = (merge_state or "").upper()
    if state == "MERGED":
        return False
    checks = rollup or []
    if not checks:
        return True
    conclusions = [(c.get("conclusion") or "").upper() for c in checks]
    if all(item == "ACTION_REQUIRED" for item in conclusions):
        return True
    return False
