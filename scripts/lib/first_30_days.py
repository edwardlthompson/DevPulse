"""Keep docs/FIRST_30_DAYS.md aligned with docs/first-30-days.json."""
from __future__ import annotations

import json
from pathlib import Path

JSON_REL = Path("docs") / "first-30-days.json"
MD_REL = Path("docs") / "FIRST_30_DAYS.md"


def load_weeks(root: Path) -> list[dict]:
    data = json.loads((root / JSON_REL).read_text(encoding="utf-8"))
    weeks = data.get("weeks")
    if not isinstance(weeks, list):
        return []
    return [item for item in weeks if isinstance(item, dict)]


def check_repo(root: Path) -> list[str]:
    md_path = root / MD_REL
    json_path = root / JSON_REL
    if not json_path.is_file():
        return [f"MISSING: {JSON_REL.as_posix()}"]
    if not md_path.is_file():
        return [f"MISSING: {MD_REL.as_posix()}"]
    text = md_path.read_text(encoding="utf-8")
    errors: list[str] = []
    weeks = load_weeks(root)
    if len(weeks) < 4:
        errors.append("first-30-days.json must list four weeks")
    for week in weeks:
        title = str(week.get("title") or "")
        if title and title not in text:
            errors.append(f"FIRST_30_DAYS.md missing week: {title}")
        for needle in week.get("needles") or []:
            if str(needle) not in text:
                errors.append(f"FIRST_30_DAYS.md missing {needle}")
    health = (root / "scripts" / "project-health.sh").read_text(encoding="utf-8")
    if JSON_REL.as_posix() not in health:
        errors.append("project-health.sh must point at docs/first-30-days.json")
    return errors


def playbook_pointer() -> str:
    return f"Playbook JSON: {JSON_REL.as_posix()}  Markdown: {MD_REL.as_posix()}"


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("FIRST_30_DAYS health JSON check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("FIRST_30_DAYS health JSON check passed")
    print(playbook_pointer())
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
