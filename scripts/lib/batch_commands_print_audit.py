"""Print-sheet a11y and Settings-only caption audit."""
from __future__ import annotations


def audit_html(html: str, cmds: dict) -> list[str]:
    errors: list[str] = []
    if 'lang="en"' not in html:
        errors.append("print HTML missing lang=en")
    if 'charset="utf-8"' not in html:
        errors.append("print HTML missing charset")
    if 'scope="col"' not in html:
        errors.append("print HTML missing th scope=col")
    if "Cline" not in html:
        errors.append("print HTML missing Cline first-run")
    if "Do not paste API keys" not in html:
        errors.append("print HTML missing API-key warning")
    if "/push" not in html or "/ship" not in html:
        errors.append("print HTML missing /push or /ship")
    header = html.split("<h2", 1)[0]
    for banned in ("Theme", "donate", "About"):
        if banned.lower() in header.lower():
            errors.append(f"print HTML header mentions {banned}")
    tour = str((cmds.get("tour") or {}).get("caption") or "")
    coach = str((cmds.get("coach") or {}).get("caption") or "")
    if "Settings-only" not in tour:
        errors.append("tour caption must mention Settings-only")
    if "Settings-only" not in coach:
        errors.append("coach caption must mention Settings-only")
    return errors
