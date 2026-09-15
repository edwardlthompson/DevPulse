"""ADR-0001 stays a child architecture-pick template (no pre-selected pattern)."""
from __future__ import annotations

from pathlib import Path

ADR = Path("docs") / "adr" / "0001-core-architecture.md"
INIT = Path("docs") / "INITIALIZATION_PROMPT.md"
PATTERNS = ("MVVM", "Clean Architecture", "Hexagonal")


from build_sprint_model import is_template_repo


def check_repo(root: Path) -> list[str]:
    if not is_template_repo(root):
        return []
    errors: list[str] = []
    path = root / ADR
    if not path.is_file():
        return [f"MISSING: {ADR.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors.extend(f"{ADR.as_posix()} must name {name}" for name in PATTERNS if name not in text)
    selected = next((line for line in text.splitlines() if "Selected pattern" in line), "")
    if not selected:
        errors.append("ADR-0001 must have a Selected pattern line")
    else:
        if selected.count("🔲") < 3:
            errors.append("ADR-0001 template must leave all three patterns open")
        if "✅" in selected:
            errors.append("ADR-0001 must not pre-select a pattern on the template")
    init = root / INIT
    if not init.is_file():
        errors.append(f"MISSING: {INIT.as_posix()}")
    elif "ADR-0001" not in init.read_text(encoding="utf-8"):
        errors.append("INITIALIZATION_PROMPT.md must name ADR-0001")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("ADR-0001 architecture-pick check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("ADR-0001 stays an open architecture pick")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
