"""Commercial Automations YAML stays an example and off the FOSS path."""
from __future__ import annotations

from pathlib import Path

EXAMPLE = Path(".cursor") / "automations.commercial.example.yaml"
LIVE = Path(".cursor") / "automations.yaml"
NEEDLES = (
    "enabled: false",
    "git push",
    "validate-bootstrap --quick",
    "untrusted",
    "Do not enable on the FOSS path",
)


def check_repo(root: Path) -> list[str]:
    path = root / EXAMPLE
    if not path.is_file():
        return [f"MISSING: {EXAMPLE.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors = [f"{EXAMPLE.as_posix()} missing {needle}" for needle in NEEDLES if needle not in text]
    if (root / LIVE).is_file():
        errors.append(f"{LIVE.as_posix()} must not be committed (FOSS path)")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Cursor automations example check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Cursor automations example stays disabled")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
