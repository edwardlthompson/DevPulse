#!/usr/bin/env python3
"""Generate platform design outputs from design-tokens/design-tokens.json.

Also distributes branding assets and writes branding/official-colors.css.
"""
from __future__ import annotations

import hashlib
import json
import re
import shutil
import sys
from pathlib import Path

HEADER = "GENERATED — do not edit; run scripts/sync-design-tokens.py"

REQUIRED_BRAND_ASSETS = (
    "logo-mark.svg",
    "logo-mark-mono.svg",
    "logo-wordmark.svg",
    "logo-lockup.svg",
    "favicon.svg",
    "app-icon-512.svg",
    "readme-hero.svg",
    "social-preview.svg",
)


def repo_root() -> Path:
    return Path(__file__).resolve().parent.parent


def load_tokens(root: Path) -> dict:
    path = root / "design-tokens" / "design-tokens.json"
    return json.loads(path.read_text(encoding="utf-8"))


def token_hash(tokens: dict) -> str:
    raw = json.dumps(tokens, sort_keys=True).encode("utf-8")
    return hashlib.sha256(raw).hexdigest()[:12]


def hex_to_compose(name: str, hex_color: str, *, private: bool = False) -> str:
    h = hex_color.lstrip("#")
    prefix = "private val " if private else "val "
    return f"{prefix}{name} = Color(0xFF{h.upper()})"


def camel_case(key: str) -> str:
    parts = key.replace("-", "_").split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def color_role_name(key: str) -> str:
    return camel_case(key)


def generate_css(tokens: dict, digest: str) -> str:
    colors = tokens["color"]
    spacing = tokens["spacing"]
    radius = tokens["radius"]
    typo = tokens["typography"]
    meta = tokens["meta"]

    def vars_block(mode: str) -> list[str]:
        lines = []
        for key, value in colors.items():
            css_key = key.replace("on", "on-").replace("Variant", "-variant")
            css_key = "--gp-color-" + _kebab(key)
            lines.append(f"  {css_key}: {value[mode]};")
        for key, value in spacing.items():
            lines.append(f"  --gp-space-{key}: {value}px;")
        for key, value in radius.items():
            lines.append(f"  --gp-radius-{key}: {value}px;")
        lines.append(f"  --gp-font-sans: {typo['fontFamily']['sans']};")
        for scale_key, scale in typo["scale"].items():
            kebab = _kebab(scale_key)
            lines.append(f"  --gp-text-{kebab}-size: {scale['sizeRem']}rem;")
            lines.append(f"  --gp-text-{kebab}-line: {scale['lineHeight']};")
            lines.append(f"  --gp-text-{kebab}-weight: {scale['weight']};")
        return lines

    light_lines = vars_block("light")
    dark_lines = vars_block("dark")

    parts = [
        f"/* {HEADER} */",
        f"/* source-hash: {digest} */",
        "",
        ":root,",
        '[data-theme="light"] {',
        *light_lines,
        "}",
        "",
        '[data-theme="dark"] {',
        *dark_lines,
        "}",
        "",
        '[data-theme="system"] {',
        *light_lines,
        "}",
        "",
        "@media (prefers-color-scheme: dark) {",
        '  [data-theme="system"] {',
        *dark_lines,
        "  }",
        "}",
        "",
    ]
    return "\n".join(parts)


def _kebab(key: str) -> str:
    out: list[str] = []
    for i, ch in enumerate(key):
        if ch.isupper() and i > 0:
            out.append("-")
        out.append(ch.lower())
    return "".join(out)


def generate_color_kt(tokens: dict, digest: str) -> str:
    colors = tokens["color"]
    light_entries = []
    dark_entries = []
    for key in colors:
        role = color_role_name(key)
        light_entries.append(hex_to_compose(f"Light{role.capitalize() if role[0].islower() else role}", colors[key]["light"]))
        dark_entries.append(hex_to_compose(f"Dark{role[0].upper()}{role[1:]}" if role else role, colors[key]["dark"]))

    # Fix naming: primary -> LightPrimary, DarkPrimary
    light_vals = []
    dark_vals = []
    scheme_light = []
    scheme_dark = []
    for key in colors:
        role = color_role_name(key)
        light_name = f"Gp{role[0].upper()}{role[1:]}"
        dark_name = light_name
        light_vals.append(hex_to_compose(f"GpLight{role[0].upper()}{role[1:]}", colors[key]["light"]))
        dark_vals.append(hex_to_compose(f"GpDark{role[0].upper()}{role[1:]}", colors[key]["dark"]))
        scheme_light.append(f"        {role} = GpLight{role[0].upper()}{role[1:]},")
        scheme_dark.append(f"        {role} = GpDark{role[0].upper()}{role[1:]},")

    lines = [
        f"// {HEADER}",
        f"// source-hash: {digest}",
        "package dev.foss.goldenpath.ui.theme",
        "",
        "import androidx.compose.material3.darkColorScheme",
        "import androidx.compose.material3.lightColorScheme",
        "import androidx.compose.ui.graphics.Color",
        "",
        "// Raw palette",
    ]
    for key in colors:
        role = color_role_name(key)
        cap = role[0].upper() + role[1:]
        lines.append(hex_to_compose(f"GpLight{cap}", colors[key]["light"], private=True))
        lines.append(hex_to_compose(f"GpDark{cap}", colors[key]["dark"], private=True))
    lines.extend([
        "",
        "val LightGoldenPathColors = lightColorScheme(",
        *[f"    {color_role_name(k)} = GpLight{color_role_name(k)[0].upper()}{color_role_name(k)[1:]}," for k in colors],
        ")",
        "",
        "val DarkGoldenPathColors = darkColorScheme(",
        *[f"    {color_role_name(k)} = GpDark{color_role_name(k)[0].upper()}{color_role_name(k)[1:]}," for k in colors],
        ")",
        "",
    ])
    return "\n".join(lines)


def generate_type_kt(tokens: dict, digest: str) -> str:
    scale = tokens["typography"]["scale"]
    entries = []
    for key, val in scale.items():
        entries.append(
            f"    {key} = TextStyle(\n"
            f"        fontSize = {val['sizeSp']}.sp,\n"
            f"        lineHeight = {(val['sizeSp'] * val['lineHeight']):.1f}.sp,\n"
            f"        fontWeight = FontWeight({val['weight']}),\n"
            f"    ),"
        )
    return "\n".join([
        f"// {HEADER}",
        f"// source-hash: {digest}",
        "package dev.foss.goldenpath.ui.theme",
        "",
        "import androidx.compose.material3.Typography",
        "import androidx.compose.ui.text.TextStyle",
        "import androidx.compose.ui.text.font.FontWeight",
        "import androidx.compose.ui.unit.sp",
        "",
        "val GoldenPathTypography = Typography(",
        *entries,
        ")",
        "",
    ])


def generate_dimens_kt(tokens: dict, digest: str) -> str:
    spacing = tokens["spacing"]
    radius = tokens["radius"]
    elevation = tokens["elevation"]
    lines = [
        f"// {HEADER}",
        f"// source-hash: {digest}",
        "package dev.foss.goldenpath.ui.theme",
        "",
        "import androidx.compose.ui.unit.dp",
        "",
    ]
    for key, val in spacing.items():
        name = key[0].upper() + key[1:]
        lines.append(f"val Spacing{name} = {val}.dp")
    lines.append("")
    for key, val in radius.items():
        name = key[0].upper() + key[1:]
        lines.append(f"val Radius{name} = {val}.dp")
    lines.append("")
    for key, val in elevation.items():
        name = key.replace("level", "Level")
        lines.append(f"val Elevation{name} = {val}.dp")
    lines.append("")
    return "\n".join(lines)


def generate_theme_meta(tokens: dict) -> str:
    meta = tokens["meta"]
    payload = {
        "themeColorLight": meta["themeColorLight"],
        "themeColorDark": meta["themeColorDark"],
        "name": meta["name"],
    }
    return json.dumps(payload, indent=2) + "\n"


def generate_official_colors_css(tokens: dict, digest: str) -> str:
    """Human-facing brand aliases generated from design tokens."""
    colors = tokens["color"]
    meta = tokens["meta"]
    primary = colors["primary"]["light"]
    secondary = colors["secondary"]["light"]
    surface = colors["surface"]["light"]
    background = colors["background"]["light"]
    on_surface = colors["onSurface"]["light"]
    lines = [
        f"/* {HEADER} */",
        f"/* source-hash: {digest} */",
        "/* Official brand colors — prefer --gp-* in app UI; use --brand-* in docs/marketing */",
        "",
        ":root {",
        f"  --brand-name: \"{meta['name']}\";",
        f"  --brand-theme-light: {meta['themeColorLight']};",
        f"  --brand-theme-dark: {meta['themeColorDark']};",
    ]
    for key, value in colors.items():
        kebab = _kebab(key)
        lines.append(f"  --brand-{kebab}: {value['light']};")
        lines.append(f"  --brand-{kebab}-dark: {value['dark']};")
    lines.extend(
        [
            "",
            "  /* Convenience aliases (light) */",
            f"  --brand-accent: {primary};",
            f"  --brand-accent-secondary: {secondary};",
            f"  --brand-canvas: {background};",
            f"  --brand-panel: {surface};",
            f"  --brand-ink: {on_surface};",
            "}",
            "",
            "@media (prefers-color-scheme: dark) {",
            "  :root {",
            f"    --brand-accent: {colors['primary']['dark']};",
            f"    --brand-accent-secondary: {colors['secondary']['dark']};",
            f"    --brand-canvas: {colors['background']['dark']};",
            f"    --brand-panel: {colors['surface']['dark']};",
            f"    --brand-ink: {colors['onSurface']['dark']};",
            "  }",
            "}",
            "",
        ]
    )
    return "\n".join(lines)


def require_brand_assets(root: Path) -> Path:
    assets = root / "branding" / "assets"
    if not assets.is_dir():
        raise FileNotFoundError("missing branding/assets/")
    missing = [name for name in REQUIRED_BRAND_ASSETS if not (assets / name).is_file()]
    if missing:
        raise FileNotFoundError(
            "missing branding assets: " + ", ".join(missing)
        )
    return assets


def _svg_fill_to_android_vector(mark_svg: str) -> str:
    """Best-effort conversion of logo-mark.svg to a VectorDrawable."""
    surface = "#1a1a2e"
    primary = "#e94560"
    rect = re.search(
        r'<rect[^>]*fill="(#[0-9A-Fa-f]{6})"',
        mark_svg,
    )
    path = re.search(
        r'<path[^>]*d="([^"]+)"[^>]*fill="(#[0-9A-Fa-f]{6})"',
        mark_svg,
    )
    if not path:
        path = re.search(
            r'<path[^>]*fill="(#[0-9A-Fa-f]{6})"[^>]*d="([^"]+)"',
            mark_svg,
        )
        if path:
            primary = path.group(1)
            d = path.group(2)
        else:
            d = "M16 40 L32 16 L48 40 Z"
    else:
        d = path.group(1)
        primary = path.group(2)
    if rect:
        surface = rect.group(1)
    # Scale 64→24 viewport for Android convention
    return "\n".join(
        [
            f"<!-- {HEADER} -->",
            '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
            '    android:width="24dp"',
            '    android:height="24dp"',
            '    android:viewportWidth="64"',
            '    android:viewportHeight="64">',
            f'    <path android:fillColor="{surface}"',
            '        android:pathData="M12,0 L52,0 A12,12 0 0 1 64,12 L64,52 A12,12 0 0 1 52,64 L12,64 A12,12 0 0 1 0,52 L0,12 A12,12 0 0 1 12,0 Z"/>',
            f'    <path android:fillColor="{primary}"',
            f'        android:pathData="{d}"/>',
            "</vector>",
            "",
        ]
    )


def distribute_web_branding(web_root: Path, assets: Path, tokens: dict) -> None:
    public = web_root / "public"
    public.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(assets / "logo-mark.svg", public / "icon.svg")
    shutil.copyfile(assets / "logo-mark.svg", public / "logo.svg")
    shutil.copyfile(assets / "favicon.svg", public / "favicon.svg")
    shutil.copyfile(assets / "readme-hero.svg", public / "readme-hero.svg")
    shutil.copyfile(assets / "social-preview.svg", public / "social-preview.svg")

    meta = tokens["meta"]
    theme_dark = meta["themeColorDark"]
    surface_dark = tokens["color"]["surface"]["dark"]
    manifest_path = public / "manifest.webmanifest"
    if manifest_path.is_file():
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        manifest["theme_color"] = theme_dark
        manifest["background_color"] = surface_dark
        icons = manifest.get("icons") or []
        # Ensure favicon + icon entries
        srcs = {i.get("src") for i in icons if isinstance(i, dict)}
        if "/icon.svg" not in srcs:
            icons.append(
                {
                    "src": "/icon.svg",
                    "sizes": "any",
                    "type": "image/svg+xml",
                    "purpose": "any",
                }
            )
        if "/favicon.svg" not in srcs:
            icons.append(
                {
                    "src": "/favicon.svg",
                    "sizes": "any",
                    "type": "image/svg+xml",
                    "purpose": "any",
                }
            )
        manifest["icons"] = icons
        manifest_path.write_text(
            json.dumps(manifest, indent=2) + "\n", encoding="utf-8"
        )

    index_path = web_root / "index.html"
    if index_path.is_file():
        html = index_path.read_text(encoding="utf-8")
        html = re.sub(
            r'(<meta name="theme-color" content=")[^"]*(")',
            rf'\g<1>{meta["themeColorLight"]}\2',
            html,
            count=1,
        )
        if 'rel="icon"' in html and "/favicon.svg" not in html:
            html = html.replace(
                '<link rel="icon" href="/icon.svg" type="image/svg+xml" />',
                '<link rel="icon" href="/favicon.svg" type="image/svg+xml" />\n'
                '    <link rel="apple-touch-icon" href="/icon.svg" />',
            )
        elif 'href="/favicon.svg"' not in html and 'rel="icon"' in html:
            pass
        else:
            if 'rel="icon"' not in html:
                html = html.replace(
                    "</head>",
                    '    <link rel="icon" href="/favicon.svg" type="image/svg+xml" />\n'
                    '    <link rel="apple-touch-icon" href="/icon.svg" />\n'
                    "  </head>",
                )
        index_path.write_text(html, encoding="utf-8", newline="\n")


def distribute_android_branding(android_root: Path, assets: Path) -> None:
    drawable = (
        android_root
        / "app"
        / "src"
        / "main"
        / "res"
        / "drawable"
    )
    drawable.mkdir(parents=True, exist_ok=True)
    mark = (assets / "logo-mark.svg").read_text(encoding="utf-8")
    (drawable / "ic_brand_mark.xml").write_text(
        _svg_fill_to_android_vector(mark), encoding="utf-8", newline="\n"
    )


def write_outputs(root: Path) -> None:
    tokens = load_tokens(root)
    digest = token_hash(tokens)
    synced: list[str] = []

    assets = require_brand_assets(root)
    brand_css = root / "branding" / "official-colors.css"
    brand_css.write_text(
        generate_official_colors_css(tokens, digest), encoding="utf-8", newline="\n"
    )
    synced.append("branding")

    web_root = root / "examples" / "web"
    if web_root.is_dir():
        web_css = web_root / "src" / "design-tokens.css"
        theme_meta = web_root / "src" / "theme-meta.json"
        web_css.parent.mkdir(parents=True, exist_ok=True)
        web_css.write_text(generate_css(tokens, digest), encoding="utf-8")
        theme_meta.write_text(generate_theme_meta(tokens), encoding="utf-8")
        distribute_web_branding(web_root, assets, tokens)
        synced.append("web")

    android_root = root / "examples" / "android"
    if android_root.is_dir():
        android_theme = (
            android_root
            / "app"
            / "src"
            / "main"
            / "java"
            / "dev"
            / "foss"
            / "goldenpath"
            / "ui"
            / "theme"
        )
        android_theme.mkdir(parents=True, exist_ok=True)
        (android_theme / "Color.kt").write_text(generate_color_kt(tokens, digest), encoding="utf-8")
        (android_theme / "Type.kt").write_text(generate_type_kt(tokens, digest), encoding="utf-8")
        (android_theme / "Dimens.kt").write_text(generate_dimens_kt(tokens, digest), encoding="utf-8")
        distribute_android_branding(android_root, assets)
        synced.append("android")

    print(f"Synced design tokens for {', '.join(synced)} (hash {digest})")


def main() -> None:
    root = repo_root()
    if not (root / "design-tokens" / "design-tokens.json").is_file():
        print("Missing design-tokens/design-tokens.json", file=sys.stderr)
        sys.exit(1)
    try:
        write_outputs(root)
    except FileNotFoundError as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
