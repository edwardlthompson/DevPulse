"""Generated README preview must include a mermaid diagram."""
from __future__ import annotations

from pathlib import Path

PREVIEW = Path("branding") / "generated" / "README.preview.md"
TEMPLATE = Path("branding") / "templates" / "README.product.md"


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    for rel in (TEMPLATE, PREVIEW):
        path = root / rel
        if not path.is_file():
            errors.append(f"MISSING: {rel.as_posix()}")
            continue
        text = path.read_text(encoding="utf-8")
        if "```mermaid" not in text:
            errors.append(f"{rel.as_posix()} missing mermaid fence")
        if "flowchart" not in text:
            errors.append(f"{rel.as_posix()} missing mermaid flowchart")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Generated README mermaid check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Generated README mermaid check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
