"""Validate OpenVEX example used next to the release SBOM."""
from __future__ import annotations

from pathlib import Path
import json

REQUIRED_TOP = ("@context", "@id", "author", "role", "timestamp", "version", "statements")
REQUIRED_STMT = ("vulnerability", "products", "status")
STATUSES = {
    "not_affected",
    "affected",
    "fixed",
    "under_investigation",
}


def validate_openvex(data: object) -> list[str]:
    errors: list[str] = []
    if not isinstance(data, dict):
        return ["root must be an object"]
    for key in REQUIRED_TOP:
        if key not in data:
            errors.append(f"missing {key}")
    ctx = data.get("@context")
    if isinstance(ctx, str) and "openvex.dev" not in ctx:
        errors.append("@context must mention openvex.dev")
    statements = data.get("statements")
    if not isinstance(statements, list) or not statements:
        errors.append("statements must be a non-empty array")
        return errors
    for i, stmt in enumerate(statements):
        if not isinstance(stmt, dict):
            errors.append(f"statements[{i}] must be an object")
            continue
        for key in REQUIRED_STMT:
            if key not in stmt:
                errors.append(f"statements[{i}] missing {key}")
        status = stmt.get("status")
        if status not in STATUSES:
            errors.append(f"statements[{i}] status {status!r} invalid")
        vuln = stmt.get("vulnerability")
        if not isinstance(vuln, dict) or not vuln.get("name"):
            errors.append(f"statements[{i}] vulnerability.name required")
        products = stmt.get("products")
        if not isinstance(products, list) or not products:
            errors.append(f"statements[{i}] products required")
    return errors


def check_repo(root: Path) -> list[str]:
    path = root / "schemas" / "golden-path" / "openvex.example.json"
    if not path.is_file():
        return ["MISSING: schemas/golden-path/openvex.example.json"]
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return [f"invalid JSON: {exc}"]
    return validate_openvex(data)


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("OpenVEX check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("OpenVEX example check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
