"""Print one-line coverage summaries from local JSON reports if present."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CANDIDATES = (
    ROOT / "examples/web/coverage/coverage-summary.json",
    ROOT / "examples/python/coverage/coverage-summary.json",
    ROOT / "examples/node/coverage/coverage-summary.json",
)


def _pct(summary: dict) -> str | None:
    total = summary.get("total") or summary.get("total", {})
    if isinstance(total, dict):
        lines = total.get("lines") or total.get("statements")
        if isinstance(lines, dict) and "pct" in lines:
            return f"{lines['pct']}%"
        if isinstance(lines, (int, float)):
            return f"{lines}%"
    if "totals" in summary and isinstance(summary["totals"], dict):
        t = summary["totals"]
        covered = t.get("covered_lines") or t.get("num_statements")
        return str(covered) if covered is not None else None
    return None


def main() -> int:
    found = 0
    for path in CANDIDATES:
        if not path.is_file():
            continue
        found += 1
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            print(f"coverage: {path.parent.parent.name}=unreadable")
            continue
        label = path.parent.parent.name
        pct = _pct(data) or "present"
        print(f"coverage: {label}={pct}")
    if found == 0:
        print("coverage: (no local reports — run stack tests with coverage)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
