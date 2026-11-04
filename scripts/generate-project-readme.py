#!/usr/bin/env python3
"""Generate pitch-quality README from branding/product.json + template.

mode=template → branding/generated/README.preview.md only
mode=product  → also writes root README.md (fails on placeholder copy)
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

PLACEHOLDER_RE = re.compile(r"\[INSERT[^\]]*\]", re.IGNORECASE)
SEED_TAGLINE = "FOSS apps with a clear path from idea to release"


def repo_root() -> Path:
    return Path(__file__).resolve().parent.parent


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def validate_product(product: dict, *, product_mode: bool) -> list[str]:
    errors: list[str] = []
    required = [
        "mode",
        "name",
        "short_name",
        "tagline",
        "pitch",
        "features",
        "quick_start",
        "install",
        "usage",
        "urls",
        "badge",
    ]
    for key in required:
        if key not in product or product[key] in (None, "", []):
            errors.append(f"missing or empty field: {key}")

    if product.get("mode") not in ("template", "product"):
        errors.append('mode must be "template" or "product"')

    features = product.get("features") or []
    if not isinstance(features, list) or len(features) < 3:
        errors.append("features must be an array with at least 3 items")

    for field in ("tagline", "pitch", "install", "usage", "name"):
        val = str(product.get(field, ""))
        if PLACEHOLDER_RE.search(val):
            errors.append(f"{field} still contains [INSERT…] placeholder")

    if product_mode:
        if product.get("tagline") == SEED_TAGLINE:
            errors.append(
                "product mode: replace seed tagline in branding/product.json"
            )
        if "agent-project-bootstrap" in str(product.get("pitch", "")).lower() and (
            product.get("name") != "Golden Path"
        ):
            pass
        pitch = str(product.get("pitch", ""))
        if len(pitch) < 40:
            errors.append("product mode: pitch must be at least 40 characters")
    return errors


def render_list(items: list[str], *, ordered: bool = False) -> str:
    if ordered:
        return "\n".join(f"{i}. {item}" for i, item in enumerate(items, 1))
    return "\n".join(f"- {item}" for item in items)


def stack_badges(product: dict) -> str:
    stacks = product.get("stacks") or []
    primary = product["badge"]["primary"]
    secondary = product["badge"]["secondary"]
    colors = [primary, secondary, "3DDC84", "3776AB", "646cff"]
    lines = []
    for i, stack in enumerate(stacks):
        color = colors[i % len(colors)]
        lines.append(
            f'  <img src="https://img.shields.io/badge/{stack}-stack-{color}'
            f'?style=flat-square" alt="{stack}" />'
        )
    if not lines:
        return ""
    return "\n" + "\n".join(lines)


def _rel_url(path: str, *, from_preview: bool) -> str:
    """Rewrite repo-root-relative paths for branding/generated/ preview."""
    if path.startswith(("http://", "https://", "mailto:", "#")):
        return path
    if not from_preview:
        return path
    # Already relative to branding/
    if path.startswith("../"):
        return path
    if path.startswith("branding/"):
        return "../" + path[len("branding/") :]
    return "../../" + path


def render_readme(root: Path, product: dict, *, for_preview: bool = False) -> str:
    template_path = root / "branding" / "templates" / "README.product.md"
    template = template_path.read_text(encoding="utf-8")
    urls = product["urls"]
    badge = product["badge"]
    if for_preview:
        hero_path = "../assets/readme-hero.svg"
        lockup_path = "../assets/logo-lockup.svg"
    else:
        hero_path = "branding/assets/readme-hero.svg"
        lockup_path = "branding/assets/logo-lockup.svg"

    replacements = {
        "{{name}}": product["name"],
        "{{tagline}}": product["tagline"],
        "{{pitch}}": product["pitch"],
        "{{features}}": render_list(product["features"]),
        "{{quick_start}}": render_list(product["quick_start"], ordered=True),
        "{{install}}": product["install"],
        "{{usage}}": product["usage"],
        "{{hero_path}}": hero_path,
        "{{lockup_path}}": lockup_path,
        "{{badge_license}}": badge["license"],
        "{{badge_foss}}": badge["foss"],
        "{{badge_primary}}": badge["primary"],
        "{{stack_badges}}": stack_badges(product),
        "{{url_contributing}}": _rel_url(urls["contributing"], from_preview=for_preview),
        "{{url_security}}": _rel_url(urls["security"], from_preview=for_preview),
        "{{url_license}}": _rel_url(urls["license"], from_preview=for_preview),
        "{{url_branding}}": _rel_url(
            urls.get("branding", "branding/BRANDING.md"), from_preview=for_preview
        ),
        "{{url_design}}": _rel_url(
            urls.get("design", "docs/DESIGN_GUIDE.md"), from_preview=for_preview
        ),
        "{{url_env_example}}": _rel_url(".env.example", from_preview=for_preview),
        "{{url_security_triage}}": _rel_url(
            "docs/SECURITY_TRIAGE.md", from_preview=for_preview
        ),
        "{{url_best_practices}}": _rel_url(
            "docs/BEST_PRACTICES.md", from_preview=for_preview
        ),
        "{{url_first_30_days}}": _rel_url(
            "docs/FIRST_30_DAYS.md", from_preview=for_preview
        ),
        "{{url_start_here}}": _rel_url("docs/START_HERE.md", from_preview=for_preview),
        "{{url_agents}}": _rel_url("AGENTS.md", from_preview=for_preview),
        "{{url_tour}}": _rel_url("docs/help/TOUR.md", from_preview=for_preview),
        "{{ci_repo}}": str(urls.get("github_repo") or "OWNER/REPO"),
        "{{license_name}}": "MIT License",
    }
    out = template
    for key, value in replacements.items():
        out = out.replace(key, value)
    leftover = re.findall(r"\{\{[a-z_]+\}\}", out)
    if leftover:
        raise ValueError(f"unresolved template placeholders: {', '.join(leftover)}")
    return out if out.endswith("\n") else out + "\n"


def main() -> int:
    root = repo_root()
    product_path = root / "branding" / "product.json"
    if not product_path.is_file():
        print("Missing branding/product.json", file=sys.stderr)
        return 1

    product = load_json(product_path)
    product_mode = product.get("mode") == "product"
    errors = validate_product(product, product_mode=product_mode)
    if errors:
        for err in errors:
            print(f"FAIL: {err}", file=sys.stderr)
        return 1

    preview = root / "branding" / "generated" / "README.preview.md"
    preview.parent.mkdir(parents=True, exist_ok=True)

    try:
        preview.write_text(
            render_readme(root, product, for_preview=True),
            encoding="utf-8",
            newline="\n",
        )

        if product_mode:
            root_readme = root / "README.md"
            root_readme.write_text(
                render_readme(root, product, for_preview=False),
                encoding="utf-8",
                newline="\n",
            )
            print(f"Wrote {preview.relative_to(root)} and README.md (product mode)")
        else:
            print(
                f"Wrote {preview.relative_to(root)} "
                "(template mode; root README unchanged)"
            )
    except (OSError, ValueError) as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
