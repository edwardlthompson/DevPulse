"""Require a Gitleaks baseline that allowlists documented fixture secrets."""
from __future__ import annotations

from pathlib import Path

CONFIG = Path(".gitleaks.toml")
REQUIRED = (
    "useDefault",
    "sanitize-fixtures",
    "[allowlist]",
    "android-local-properties-sdk-dir",
    "sdk\\.dir",
)


def check_repo(root: Path) -> list[str]:
    path = root / CONFIG
    if not path.is_file():
        return [f"MISSING: {CONFIG.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors = [f"missing snippet: {snip}" for snip in REQUIRED if snip not in text]
    security = (root / ".github" / "workflows" / "security.yml").read_text(encoding="utf-8")
    if "gitleaks" not in security.lower():
        errors.append("security.yml must run gitleaks")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Gitleaks baseline check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Gitleaks baseline check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
