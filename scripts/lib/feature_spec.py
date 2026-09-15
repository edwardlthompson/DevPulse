"""Require feature specs to name tests or a fallback validation command."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
H2 = re.compile(r"^## (.+)$", re.M)
AUTO = re.compile(r"Automated:\s*(yes|no)\b", re.I)


def h2_headings(text: str) -> list[str]:
    return [m.group(1).strip() for m in H2.finditer(text)]


def section_after(text: str, title: str) -> str:
    heads = list(H2.finditer(text))
    for i, match in enumerate(heads):
        if match.group(1).strip() == title:
            start = match.end()
            end = heads[i + 1].start() if i + 1 < len(heads) else len(text)
            return text[start:end]
    return ""


def check_text(text: str, rel: str, contract: dict) -> list[str]:
    errors: list[str] = []
    headings = h2_headings(text)
    for title in contract["required_h2"]:
        if title not in headings:
            errors.append(f"{rel}: missing ## {title}")
    tests = section_after(text, "Tests")
    auto = AUTO.search(tests) if tests else None
    if tests and not auto:
        errors.append(f"{rel}: Tests must include 'Automated: yes' or 'Automated: no'")
    fallback = section_after(text, "Fallback validation")
    if fallback:
        if "`" not in fallback:
            errors.append(f"{rel}: Fallback validation must name a command in backticks")
        needles = contract["command_must_contain"]
        if not any(n in fallback for n in needles):
            errors.append(f"{rel}: Fallback validation command must mention {needles}")
        if auto and auto.group(1).lower() == "no":
            if re.search(r"Why tests are not feasible:\s*N/A", fallback, re.I):
                errors.append(f"{rel}: Automated: no requires a real 'why tests are not feasible'")
    return errors


def check_repo(root: Path | None = None) -> list[str]:
    base = root or ROOT
    contract = json.loads(
        (base / "schemas/features/feature-spec.contract.json").read_text(encoding="utf-8")
    )
    skip = set(contract["skip_basenames"])
    errors: list[str] = []
    for path in sorted((base / "docs" / "features").glob("*.md")):
        if path.name in skip:
            continue
        rel = path.relative_to(base).as_posix()
        errors.extend(check_text(path.read_text(encoding="utf-8"), rel, contract))
    return errors


def main() -> int:
    errors = check_repo()
    if errors:
        print("\n".join(errors))
        return 1
    print("Feature spec contract passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
