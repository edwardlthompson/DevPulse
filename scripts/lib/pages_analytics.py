"""Fail if the GitHub Pages web tree contains analytics / trackers."""
from __future__ import annotations

from pathlib import Path

NEEDLES = (
    "googletagmanager",
    "google-analytics",
    "gtag(",
    "ga('create'",
    "plausible.io",
    "cdn.usefathom",
    "mixpanel",
    "hotjar.com",
    "segment.com/analytics",
    "connect.facebook.net",
    "_paq.push",
    "goatcounter",
    "static.cloudflareinsights",
    "umami.track",
    "vite_analytics",
)
SKIP_PARTS = ("node_modules", "dist-ssr", ".vite")
SKIP_SUFFIXES = (".test.ts", ".test.js", ".spec.ts", ".spec.js")
SCAN = (
    Path("examples/web/index.html"),
    Path("examples/web/src"),
    Path("examples/web/public"),
    Path("examples/web/dist"),
)


def _iter_files(root: Path) -> list[Path]:
    out: list[Path] = []
    for rel in SCAN:
        path = root / rel
        if path.is_file():
            out.append(path)
        elif path.is_dir():
            for child in path.rglob("*"):
                if not child.is_file() or any(p in child.parts for p in SKIP_PARTS):
                    continue
                if child.name.endswith(SKIP_SUFFIXES):
                    continue
                out.append(child)
    return out


def scan_text(text: str) -> list[str]:
    lower = text.lower()
    return [n for n in NEEDLES if n in lower]


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    for path in _iter_files(root):
        try:
            text = path.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        hits = scan_text(text)
        if hits:
            rel = path.relative_to(root).as_posix()
            errors.append(f"{rel}: {', '.join(hits)}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Pages analytics check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Pages analytics check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
