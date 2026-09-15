"""Keep Lighthouse accessibility, best-practices, and web-vitals floors from regressing."""

from __future__ import annotations

import json
import sys
from pathlib import Path

# Re-baselined after Settings-only IA (M58 #22): keep error floors, do not warn-only.
FLOORS = {
    "categories:performance": 0.9,
    "categories:accessibility": 0.95,
    "categories:best-practices": 0.9,
}

# Core Web Vitals budgets (ms) — LCP + TBT (lab INP proxy; navigation-mode LH has no INP).
VITALS = {
    "largest-contentful-paint": 2500,
    "total-blocking-time": 300,
}


def check(root: Path) -> list[str]:
    path = root / "examples/web/.lighthouserc.json"
    if not path.is_file():
        return []
    data = json.loads(path.read_text(encoding="utf-8"))
    assertions = data.get("ci", {}).get("assert", {}).get("assertions", {})
    errors: list[str] = []
    for key, floor in FLOORS.items():
        spec = assertions.get(key)
        if not isinstance(spec, list) or len(spec) < 2 or spec[0] != "error":
            errors.append(f"{key} must be an error assertion")
            continue
        score = spec[1].get("minScore") if isinstance(spec[1], dict) else None
        if not isinstance(score, (int, float)) or score < floor:
            errors.append(f"{key} minScore must be >= {floor}, got {score}")
    for key, budget in VITALS.items():
        spec = assertions.get(key)
        if not isinstance(spec, list) or len(spec) < 2 or spec[0] != "error":
            errors.append(f"{key} must be an error assertion with maxNumericValue")
            continue
        value = spec[1].get("maxNumericValue") if isinstance(spec[1], dict) else None
        if not isinstance(value, (int, float)) or value > budget:
            errors.append(f"{key} maxNumericValue must be <= {budget}, got {value}")
    return errors


def main() -> int:
    errors = check(Path.cwd())
    if errors:
        print("\n".join(errors))
        return 1
    print("Lighthouse performance/a11y/best-practices/TBT/LCP floors passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
