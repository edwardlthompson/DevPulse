"""Require docs/PACKAGE_ATTESTATION.md to cover npm, uv, and GitHub provenance."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "PACKAGE_ATTESTATION.md"
REQUIRED = (
    "npm publish --provenance",
    "npm audit signatures",
    "uv sync --frozen",
    "PEP 740",
    "attest-build-provenance",
    "gh attestation verify",
)


def check_repo(root: Path) -> list[str]:
    path = root / DOC
    if not path.is_file():
        return [f"MISSING: {DOC.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors = [f"missing snippet: {snip}" for snip in REQUIRED if snip not in text]
    release = root / ".github" / "workflows" / "release.yml"
    if release.is_file() and "attest-build-provenance" not in release.read_text(
        encoding="utf-8"
    ):
        errors.append("release.yml lost attest-build-provenance")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Package attestation docs check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Package attestation docs check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
