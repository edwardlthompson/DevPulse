"""docs/WINGET.md must stay the publish runbook."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "WINGET.md"
EXAMPLE = Path("packaging") / "winget" / "example" / "manifest.yaml"
NEEDLES = (
    "generate-winget-manifest.sh",
    "validate-winget-stub.sh",
    "InstallerSha256",
    "microsoft/winget-pkgs",
    "[HUMAN]",
    "packaging/winget/example/manifest.yaml",
    "arm64",
    "x64",
    "winget-publish-loop.sh",
)
EXAMPLE_KEYS = (
    "PackageIdentifier:",
    "PackageVersion:",
    "ManifestVersion:",
    "License:",
    "InstallerSha256:",
)


def check_example(root: Path) -> list[str]:
    path = root / EXAMPLE
    if not path.is_file():
        return [f"MISSING: {EXAMPLE.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors = [f"{EXAMPLE.as_posix()} missing {key}" for key in EXAMPLE_KEYS if key not in text]
    if "0000000000000000000000000000000000000000000000000000000000000000" not in text:
        errors.append(f"{EXAMPLE.as_posix()} must use the placeholder SHA-256")
    if "example.com" not in text:
        errors.append(f"{EXAMPLE.as_posix()} must use an example.com InstallerUrl")
    if "Architecture: x64" not in text or "Architecture: arm64" not in text:
        errors.append(f"{EXAMPLE.as_posix()} must list x64 and arm64 installers")
    return errors


def check_repo(root: Path) -> list[str]:
    path = root / DOC
    if not path.is_file():
        return [f"MISSING: {DOC.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors = [f"{DOC.as_posix()} missing {needle}" for needle in NEEDLES if needle not in text]
    errors.extend(check_example(root))
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Winget runbook check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Winget runbook check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
