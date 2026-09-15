"""OG social preview SVG must use design-token colors."""
from __future__ import annotations

import json
from pathlib import Path

ASSET = Path("branding") / "assets" / "social-preview.svg"
PUBLIC = Path("examples") / "web" / "public" / "social-preview.svg"
INDEX = Path("examples") / "web" / "index.html"
TOKENS = Path("design-tokens") / "design-tokens.json"


def _hexes(tokens: dict) -> list[str]:
    color = tokens["color"]
    return [
        str(color["background"]["dark"]),
        str(color["surface"]["dark"]),
        str(color["primary"]["dark"]),
        str(color["onBackground"]["dark"]),
    ]


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    token_path = root / TOKENS
    if not token_path.is_file():
        return [f"MISSING: {TOKENS.as_posix()}"]
    tokens = json.loads(token_path.read_text(encoding="utf-8"))
    asset = root / ASSET
    public = root / PUBLIC
    if not asset.is_file():
        errors.append(f"MISSING: {ASSET.as_posix()}")
        return errors
    text = asset.read_text(encoding="utf-8")
    if 'viewBox="0 0 1280 640"' not in text:
        errors.append("social-preview.svg must be 1280x640")
    for hex_color in _hexes(tokens):
        if hex_color.lower() not in text.lower():
            errors.append(f"{ASSET.as_posix()} missing token color {hex_color}")
    if not (root / "examples" / "web").is_dir():
        return errors
    if not public.is_file():
        errors.append(f"MISSING: {PUBLIC.as_posix()}")
    elif public.read_bytes() != asset.read_bytes():
        errors.append("web public social-preview.svg must match branding asset")
    index = root / INDEX
    if not index.is_file():
        errors.append(f"MISSING: {INDEX.as_posix()}")
    else:
        html = index.read_text(encoding="utf-8")
        if 'property="og:image"' not in html or "social-preview.svg" not in html:
            errors.append("examples/web/index.html must set og:image to social-preview.svg")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Social preview token check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Social preview token check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
