"""Classify commands against Auto-review permissions.json fixtures."""
from __future__ import annotations

import json
from pathlib import Path

PERMS = Path(".cursor") / "permissions.json"
FIXTURES = Path("tests") / "fixtures" / "auto-review.json"

# (expect, command substring, permissions.json substring)
RULES: tuple[tuple[str, str, str], ...] = (
    ("block", "git push", "git push"),
    ("block", "terraform apply", "terraform apply"),
    ("block", "drop table", "drop table"),
    ("block", "delete from", "delete"),
    ("block", "--no-verify", "hooks"),
    ("block", ".env", ".env"),
    ("allow", "validate-bootstrap", "validate-bootstrap"),
    ("allow", "check-repo-hygiene", "check-repo-hygiene"),
    ("allow", "feature-gate", "feature-gate"),
    ("allow", "watch-agent-gates", "watch-agent-gates"),
    ("allow", "check-cursor-integrations", "check-cursor-integrations"),
    ("allow", "check-file-encoding", "check-file-encoding"),
)


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def permissions_text(root: Path) -> str:
    data = load_json(root / PERMS)
    auto = data.get("autoRun") or {}
    parts = list(auto.get("allow_instructions") or [])
    parts.extend(auto.get("block_instructions") or [])
    return "\n".join(str(item) for item in parts).lower()


def classify(command: str) -> str:
    lower = command.lower()
    for expect, needle, _perm in RULES:
        if expect == "block" and needle in lower:
            return "block"
    for expect, needle, _perm in RULES:
        if expect == "allow" and needle in lower:
            return "allow"
    return "unknown"


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    perms = root / PERMS
    fixtures = root / FIXTURES
    if not perms.is_file():
        return [f"MISSING: {PERMS.as_posix()}"]
    if not fixtures.is_file():
        return [f"MISSING: {FIXTURES.as_posix()}"]
    text = permissions_text(root)
    for expect, _needle, perm in RULES:
        if perm not in text:
            errors.append(f"permissions.json missing {expect} cue: {perm}")
    data = load_json(fixtures)
    cases = data.get("cases")
    if not isinstance(cases, list) or not cases:
        errors.append("auto-review fixtures must list cases")
        return errors
    seen: set[str] = set()
    for case in cases:
        command = str(case.get("command") or "")
        expect = str(case.get("expect") or "")
        cid = str(case.get("id") or command)
        got = classify(command)
        if got != expect:
            errors.append(f"{cid}: expected {expect}, got {got}")
        seen.add(expect)
    if "allow" not in seen or "block" not in seen:
        errors.append("fixtures must cover both allow and block")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Auto-review fixtures check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Auto-review fixtures check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
