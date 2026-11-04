"""Require crash-payload allowlist tests on every sanitizing stack."""
from __future__ import annotations

import json
from pathlib import Path

ALLOWLIST = Path("schemas") / "golden-path" / "crash-payload-allowlist.json"
SCHEMA = Path("schemas") / "golden-path" / "crash-report.schema.json"
STACK_TESTS = (
    Path("examples/web/src/crash-capture/pendingCrash.test.ts"),
    Path("examples/android/app/src/test/java/dev/foss/goldenpath/crashcapture/PendingCrashTest.kt"),
    Path("examples/python/tests/test_crash.py"),
    Path("examples/node/src/crash.test.ts"),
    Path("examples/rust/src/crash.rs"),
    Path("examples/go/about_test.go"),
)


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    allow_path = root / ALLOWLIST
    if not allow_path.is_file():
        return [f"MISSING: {ALLOWLIST.as_posix()}"]
    data = json.loads(allow_path.read_text(encoding="utf-8"))
    allowed = data.get("allowed_keys") or []
    rejected = data.get("rejected_keys") or []
    schema = json.loads((root / SCHEMA).read_text(encoding="utf-8"))
    if schema.get("additionalProperties") is not False:
        errors.append("crash-report.schema.json must set additionalProperties false")
    if list(schema.get("required") or []) != allowed:
        errors.append("schema required keys must match allowlist")
    for rel in STACK_TESTS:
        parts = rel.parts
        if len(parts) >= 2 and parts[0] == "examples" and not (root / parts[0] / parts[1]).is_dir():
            continue
        path = root / rel
        if not path.is_file():
            errors.append(f"MISSING: {rel.as_posix()}")
            continue
        text = path.read_text(encoding="utf-8")
        for key in rejected:
            if key not in text:
                errors.append(f"{rel.as_posix()} must test rejected key {key}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Crash payload allowlist check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Crash payload allowlist check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
