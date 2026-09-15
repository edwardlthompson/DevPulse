"""Named GitHub required checks (not a content-hash cache)."""
from __future__ import annotations

import json
import sys
from pathlib import Path

REL = Path(".github") / "required-checks.json"


def _load(root: Path) -> dict:
    data = json.loads((root / REL).read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError("required-checks.json must be an object")
    return data


def load_names(root: Path) -> list[str]:
    names = _load(root).get("required_status_checks")
    if not isinstance(names, list) or not names:
        raise ValueError("required_status_checks must be a non-empty list")
    return [str(item) for item in names]


def load_informational(root: Path) -> list[str]:
    names = _load(root).get("informational_status_checks") or []
    if not isinstance(names, list):
        raise ValueError("informational_status_checks must be a list")
    return [str(item) for item in names]


def main() -> int:
    root = Path.cwd()
    if "--json" in sys.argv:
        print(
            json.dumps(
                {
                    "required": load_names(root),
                    "informational": load_informational(root),
                    "map_doc": _load(root).get("map_doc"),
                }
            )
        )
        return 0
    if "--informational" in sys.argv:
        print("\n".join(load_informational(root)))
        return 0
    print("\n".join(load_names(root)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
