"""WCAG 2.2 contrast for paired design-token colors."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOKENS = ROOT / "design-tokens" / "design-tokens.json"
PAIRS = (
    ("primary", "onPrimary", 4.5),
    ("primaryContainer", "onPrimaryContainer", 4.5),
    ("secondary", "onSecondary", 4.5),
    ("secondaryContainer", "onSecondaryContainer", 4.5),
    ("tertiary", "onTertiary", 4.5),
    ("error", "onError", 4.5),
    ("background", "onBackground", 4.5),
    ("surface", "onSurface", 4.5),
    ("surfaceVariant", "onSurfaceVariant", 4.5),
    ("outline", "surface", 3.0),
    # Settings dropdowns (web `.gp-settings-row select`): onSurface on surface, both modes.
    ("onSurface", "surface", 4.5),
    ("onSurfaceVariant", "surfaceVariant", 4.5),
)


def srgb_channel(value: float) -> float:
    return value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4


def hex_to_rgb(color: str) -> tuple[float, float, float]:
    raw = color.strip().lstrip("#")
    if len(raw) != 6:
        raise ValueError(f"expected #RRGGBB, got {color!r}")
    red = int(raw[0:2], 16) / 255.0
    green = int(raw[2:4], 16) / 255.0
    blue = int(raw[4:6], 16) / 255.0
    return (red, green, blue)


def relative_luminance(color: str) -> float:
    r, g, b = (srgb_channel(c) for c in hex_to_rgb(color))
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast_ratio(fg: str, bg: str) -> float:
    lighter, darker = sorted((relative_luminance(fg), relative_luminance(bg)), reverse=True)
    return (lighter + 0.05) / (darker + 0.05)


def check_tokens(data: dict) -> list[str]:
    colors = data.get("color") or {}
    errors: list[str] = []
    for fg_name, bg_name, minimum in PAIRS:
        fg = colors.get(fg_name) or {}
        bg = colors.get(bg_name) or {}
        for mode in ("light", "dark"):
            if mode not in fg or mode not in bg:
                errors.append(f"missing {fg_name}/{bg_name} {mode}")
                continue
            ratio = contrast_ratio(fg[mode], bg[mode])
            if ratio < minimum:
                errors.append(
                    f"{fg_name}/{bg_name} {mode}: {ratio:.2f}:1 < {minimum}:1 "
                    f"({fg[mode]} on {bg[mode]})"
                )
    return errors


def check_repo(root: Path | None = None) -> list[str]:
    base = root or ROOT
    path = base / "design-tokens" / "design-tokens.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    return check_tokens(data)


def main() -> int:
    errors = check_repo()
    if errors:
        print("\n".join(errors))
        return 1
    print("WCAG token contrast passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
