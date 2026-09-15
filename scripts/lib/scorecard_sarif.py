"""Classify OpenSSF Scorecard SARIF checks into fix / dismiss / defer."""

from __future__ import annotations

import json
import sys
from pathlib import Path

# Mirrors docs/SECURITY_TRIAGE.md SARIF triage table.
RULES: dict[str, tuple[str, str]] = {
    "pinneddependencies": ("dismiss", "HUMAN"),
    "tokenpermissions": ("fix", "AGENT"),
    "vulnerabilities": ("dismiss", "HUMAN"),
    "vulnerabilitiesid": ("dismiss", "HUMAN"),
    "codereview": ("defer", "HUMAN"),
    "maintained": ("defer", "HUMAN"),
    "ciibestpractices": ("defer", "HUMAN"),
    "fuzzing": ("defer", "HUMAN"),
    "binaryartifacts": ("defer", "HUMAN"),
}


def _key(name: str) -> str:
    return "".join(ch for ch in name.lower() if ch.isalnum())


def classify_rule(name: str) -> tuple[str, str]:
    return RULES.get(_key(name), ("defer", "HUMAN"))


def result_names(sarif: dict) -> list[str]:
    names: list[str] = []
    for run in sarif.get("runs") or []:
        rules = {r.get("id"): r.get("name") or r.get("id") for r in (run.get("tool") or {}).get("driver", {}).get("rules") or []}
        for item in run.get("results") or []:
            rid = str(item.get("ruleId") or "")
            names.append(str(rules.get(rid) or rid or "unknown"))
    return names


def classify(sarif: dict) -> list[dict[str, str]]:
    rows = []
    for name in result_names(sarif):
        action, owner = classify_rule(name)
        rows.append({"check": name, "action": action, "owner": owner})
    return rows


def check_docs(root: Path) -> list[str]:
    text = (root / "docs/SECURITY_TRIAGE.md").read_text(encoding="utf-8")
    errors = []
    for needle in ("PinnedDependencies", "TokenPermissions", "VulnerabilitiesID", "BinaryArtifacts"):
        if needle not in text:
            errors.append(f"SECURITY_TRIAGE.md must mention {needle}")
    return errors


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    if args and args[0] == "--docs":
        errors = check_docs(Path.cwd())
        if errors:
            print("\n".join(errors))
            return 1
        print("Scorecard SARIF classifier docs aligned")
        return 0
    path = Path(args[0] if args else "results.sarif")
    if not path.is_file():
        print(f"missing SARIF: {path}", file=sys.stderr)
        return 2
    rows = classify(json.loads(path.read_text(encoding="utf-8")))
    print(json.dumps(rows, indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
