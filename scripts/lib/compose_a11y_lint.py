"""Require Android accessibility lint IDs on the Golden Path app."""

from __future__ import annotations

import sys
from pathlib import Path

REQUIRED = (
    "ContentDescription",
    "ClickableViewAccessibility",
    "LabelFor",
    "KeyboardInaccessibleWidget",
)

SCREEN = Path("examples/android/app/src/main/java/dev/foss/goldenpath/ui/GoldenPathScreen.kt")


def check(root: Path) -> list[str]:
    lint_xml = root / "examples/android/app/lint.xml"
    gradle = root / "examples/android/app/build.gradle.kts"
    if not lint_xml.is_file() or not gradle.is_file():
        return []
    errors: list[str] = []
    xml = lint_xml.read_text(encoding="utf-8")
    kts = gradle.read_text(encoding="utf-8")
    if "lintConfig = file(\"lint.xml\")" not in kts and "lintConfig = file('lint.xml')" not in kts:
        errors.append("app/build.gradle.kts must set lintConfig to lint.xml")
    if "abortOnError = true" not in kts:
        errors.append("app/build.gradle.kts lint { abortOnError = true } is required")
    for issue in REQUIRED:
        if f'id="{issue}"' not in xml:
            errors.append(f"lint.xml missing error issue {issue}")
        if f'error += "{issue}"' not in kts:
            errors.append(f"build.gradle.kts lint.error missing {issue}")

    screen = root / SCREEN
    if screen.is_file():
        src = screen.read_text(encoding="utf-8")
        if "if (!atHome)" not in src:
            errors.append("GoldenPathScreen.kt must gate Back icon with if (!atHome)")
        if "R.string.nav_back" not in src or "contentDescription" not in src:
            errors.append(
                "GoldenPathScreen.kt must set Back contentDescription to stringResource(R.string.nav_back)"
            )
        # Back description must appear inside the !atHome navigationIcon branch.
        if "if (!atHome)" in src:
            after = src.split("if (!atHome)", 1)[1]
            branch = after.split("actions =", 1)[0]
            if "nav_back" not in branch or "contentDescription" not in branch:
                errors.append(
                    "Back contentDescription (nav_back) must live inside the !atHome navigationIcon branch"
                )
    return errors


def main() -> int:
    root = Path.cwd()
    errors = check(root)
    if errors:
        print("\n".join(errors))
        return 1
    print("Compose a11y lint config passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
