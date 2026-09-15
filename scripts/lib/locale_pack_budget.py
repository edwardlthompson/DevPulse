"""Fail if locale JSON packs exceed the static-data line/byte budget."""
from __future__ import annotations

from pathlib import Path

MAX_BYTES = 48_000
MAX_LINES = 300
LOCALES = Path("examples/web/src/locales")


def check_repo(root: Path) -> list[str]:
    folder = root / LOCALES
    if not folder.is_dir():
        return []
    errors: list[str] = []
    total = 0
    for path in sorted(folder.glob("*.json")):
        text = path.read_text(encoding="utf-8")
        lines = text.count("\n") + 1
        total += len(text.encode("utf-8"))
        if lines > MAX_LINES:
            errors.append(f"{path.relative_to(root).as_posix()}: {lines} lines > {MAX_LINES}")
    if total > MAX_BYTES:
        errors.append(f"locale pack total {total} bytes > {MAX_BYTES}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Locale pack budget failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Locale pack budget passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
