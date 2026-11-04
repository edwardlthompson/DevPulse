"""Fail when Golden Path chrome puts theme/About/donate in the header or uses chips for theme."""
from __future__ import annotations

import re
import sys
from pathlib import Path

BANNED_FILES = (
    "examples/web/src/components/ThemeToggle.ts",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/components/ThemeToggle.kt",
)
CHIP_THEME = re.compile(r"FilterChip|ThemeToggle")
WEB_HEADER_BAD = re.compile(
    r"gp-header-actions[\s\S]{0,800}?(data-about-open|data-donate|ThemeToggle|data-theme-toggle)",
    re.I,
)
ANDROID_BAR_BAD = re.compile(
    r"TopAppBar\([\s\S]{0,2000}?actions\s*=\s*\{([\s\S]{0,1200}?)\}",
    re.I,
)
ANDROID_BAR_ACTION_BAD = re.compile(
    r"Icons\.(?:Filled|Outlined|AutoMirrored\.\w+)\.(?:Info|Favorite|DarkMode|LightMode|AttachMoney)",
    re.I,
)


def check_root(root: Path) -> list[str]:
    errors: list[str] = []
    for rel in BANNED_FILES:
        if (root / rel).is_file():
            errors.append(f"banned chrome file {rel}")
    web_shell = root / "examples/web/src/AppShell.ts"
    if web_shell.is_file():
        text = web_shell.read_text(encoding="utf-8")
        if "ThemeToggle" in text or "data-theme-toggle" in text:
            errors.append("web header must not mount ThemeToggle")
        if WEB_HEADER_BAD.search(text):
            errors.append("web header actions must be Settings-only")
        if "data-settings-open" not in text:
            errors.append("web home chrome must expose data-settings-open")
    web_settings = root / "examples/web/src/components/SettingsPanel.ts"
    if web_settings.is_file():
        text = web_settings.read_text(encoding="utf-8")
        if "chip" in text.lower() and "theme" in text.lower() and "FilterChip" in text:
            errors.append("web Settings must not use chips for theme")
        if "data-settings-theme" not in text:
            errors.append("web Settings must use data-settings-theme dropdown")
    android_bar = root / "examples/android/app/src/main/java/dev/foss/goldenpath/ui/GoldenPathScreen.kt"
    if android_bar.is_file():
        text = android_bar.read_text(encoding="utf-8")
        block = ANDROID_BAR_BAD.search(text)
        if block and ANDROID_BAR_ACTION_BAD.search(block.group(1)):
            errors.append("Android TopAppBar actions must be Settings-only")
        if "Icons.Filled.Settings" not in text:
            errors.append("Android home chrome must use Settings icon")
    settings_kt = (
        root / "examples/android/app/src/main/java/dev/foss/goldenpath/ui/settings/SettingsScreen.kt"
    )
    if settings_kt.is_file():
        text = settings_kt.read_text(encoding="utf-8")
        if CHIP_THEME.search(text):
            errors.append("Android Settings must not use FilterChip/ThemeToggle")
        if "ExposedDropdownMenu" not in text:
            errors.append("Android Settings theme must be a dropdown")
    return errors


def main() -> int:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    errors = check_root(root)
    for item in errors:
        print(f"DESIGN: {item}")
    if errors:
        print(f"{len(errors)} chrome/chip regression check(s) failed")
        return 1
    print("Chrome/chip regression check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
