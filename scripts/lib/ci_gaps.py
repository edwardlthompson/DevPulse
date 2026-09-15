"""Living ci-gap registry stays aligned with the issue form."""
from __future__ import annotations

import json
from pathlib import Path

REGISTRY = Path("schemas") / "ci-gaps.json"
DOC = Path("docs") / "CI_GAPS.md"
ISSUE = Path(".github") / "ISSUE_TEMPLATE" / "template_improvement.yml"
REQUIRED_IDS = (
    "nix-not-required-by-ci-ok",
    "android-emulator-skip-without-sdk",
    "semgrep-local-skip",
    "pr-ci-main-only",
)
STATUSES = frozenset({"accepted", "open", "skip"})


def _as_dict(raw: object) -> dict:
    return raw if isinstance(raw, dict) else {}


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    path = root / REGISTRY
    if not path.is_file():
        return [f"MISSING: {REGISTRY.as_posix()}"]
    data = _as_dict(json.loads(path.read_text(encoding="utf-8")))
    if data.get("issue_label") != "ci-gap":
        errors.append("ci-gaps.json issue_label must be ci-gap")
    gaps = data.get("gaps") or []
    ids: list[str] = []
    for item in gaps:
        row = _as_dict(item)
        gid = str(row.get("id") or "")
        if not gid or not str(row.get("note") or "").strip():
            errors.append("each ci-gap needs id and note")
            continue
        if row.get("status") not in STATUSES:
            errors.append(f"{gid}: status must be accepted|open|skip")
        ids.append(gid)
    missing = [gid for gid in REQUIRED_IDS if gid not in ids]
    if missing:
        errors.append(f"ci-gaps.json missing required ids: {missing}")
    issue = root / ISSUE
    if not issue.is_file():
        errors.append(f"MISSING: {ISSUE.as_posix()}")
    elif "ci-gap" not in issue.read_text(encoding="utf-8"):
        errors.append("template_improvement.yml must offer ci-gap")
    doc = root / DOC
    if not doc.is_file():
        errors.append(f"MISSING: {DOC.as_posix()}")
    else:
        text = doc.read_text(encoding="utf-8")
        if "schemas/ci-gaps.json" not in text or "ci-gap" not in text:
            errors.append("docs/CI_GAPS.md must point at the registry and label")
    ci = (root / ".github" / "workflows" / "ci.yml").read_text(encoding="utf-8")
    if "branches: [main]" not in ci:
        errors.append("ci.yml must keep pull_request branches [main]")
    ok_block = ci.split("ci-ok:", 1)[-1]
    if "\n      - nix" in ok_block.split("steps:", 1)[0]:
        errors.append("ci-ok must not needs nix")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("CI gap registry check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("CI gap registry aligned")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
