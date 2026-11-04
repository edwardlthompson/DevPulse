"""Check README Pages demo badge/link points at the expected GitHub Pages URL."""
from __future__ import annotations

import json
import re
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def expected_pages_url(root: Path | None = None) -> str | None:
    base = root or ROOT
    product = base / "branding" / "product.json"
    if not product.is_file():
        return None
    try:
        data = json.loads(product.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return None
    repo = (data.get("urls") or {}).get("github_repo") or ""
    if "/" not in repo:
        return None
    owner, name = repo.split("/", 1)
    return f"https://{owner}.github.io/{name}/"


def check_readme(root: Path | None = None) -> list[str]:
    base = root or ROOT
    url = expected_pages_url(base)
    if not url:
        return ["missing branding/product.json urls.github_repo"]
    readme = (base / "README.md").read_text(encoding="utf-8")
    errors: list[str] = []
    if url.rstrip("/") not in readme and url not in readme:
        errors.append(f"README.md must link the Pages demo URL ({url})")
    if "pages-demo" not in readme.lower() and "GitHub Pages" not in readme:
        errors.append("README.md must mention GitHub Pages demo")
    return errors


def probe(url: str, timeout: float = 8.0) -> str:
    req = urllib.request.Request(url, method="HEAD", headers={"User-Agent": "agent-project-bootstrap-pages-check"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return f"HTTP {getattr(resp, 'status', 200)}"
    except urllib.error.HTTPError as exc:
        return f"HTTP {exc.code}"
    except Exception as exc:  # noqa: BLE001 — soft health probe
        return f"unreachable ({exc.__class__.__name__})"


def main() -> int:
    import sys

    errors = check_readme()
    url = expected_pages_url()
    if errors:
        print("Pages demo link health failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    assert url is not None
    status = probe(url)
    print(f"Pages demo URL in README: {url}")
    print(f"Probe: {status}")
    # Soft: do not fail the gate on network; link presence is the hard check.
    if status.startswith("HTTP 4") or status.startswith("HTTP 5"):
        print("NOTE: Pages returned an error status — confirm workflow pages.yml has run on main")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
