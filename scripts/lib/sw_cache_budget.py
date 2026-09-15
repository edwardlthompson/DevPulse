"""Offline service-worker precache budget for /gates canvas and feature-gate."""

from __future__ import annotations

import os
import re
import sys
from pathlib import Path

DEFAULT_MAX_BYTES = 256 * 1024  # 256 KiB shell precache
PRECACHE_RE = re.compile(r"const\s+PRECACHE\s*=\s*\[(.*?)\];", re.S)
ENTRY_RE = re.compile(r"[\"']([^\"']+)[\"']")


def max_bytes() -> int:
    raw = os.environ.get("SW_CACHE_MAX_BYTES", "").strip()
    if raw.isdigit():
        return int(raw)
    return DEFAULT_MAX_BYTES


def parse_precache(sw_text: str) -> list[str]:
    match = PRECACHE_RE.search(sw_text)
    if not match:
        return []
    return ENTRY_RE.findall(match.group(1))


def resolve_entry(web: Path, entry: str) -> Path | None:
    rel = entry.lstrip("./") or "."
    if rel in {".", ""}:
        candidates = [web / "index.html", web / "dist" / "index.html"]
    else:
        candidates = [
            web / "public" / rel,
            web / rel,
            web / "dist" / rel,
        ]
    for path in candidates:
        if path.is_file():
            return path
    return None


def measure(root: Path) -> tuple[int, list[tuple[str, int]], list[str]]:
    """Return (total_bytes, [(entry, size)], errors)."""
    web = root / "examples" / "web"
    sw = web / "public" / "sw.js"
    errors: list[str] = []
    if not sw.is_file():
        return 0, [], []
    entries = parse_precache(sw.read_text(encoding="utf-8"))
    if not entries:
        return 0, [], ["PRECACHE list missing in public/sw.js"]
    sizes: list[tuple[str, int]] = []
    total = 0
    for entry in entries:
        path = resolve_entry(web, entry)
        if path is None:
            errors.append(f"missing precache asset: {entry}")
            continue
        size = path.stat().st_size
        sizes.append((entry, size))
        total += size
    return total, sizes, errors


def summary_row(root: Path) -> tuple[str, str] | None:
    web_sw = root / "examples" / "web" / "public" / "sw.js"
    if not web_sw.is_file():
        return None
    total, _sizes, errors = measure(root)
    budget = max_bytes()
    if errors:
        return ("sw-cache-budget", f"Fail ({'; '.join(errors[:2])})")
    if total > budget:
        return ("sw-cache-budget", f"Fail ({total}B > {budget}B)")
    return ("sw-cache-budget", f"Pass ({total}B / {budget}B)")


def check(root: Path) -> list[str]:
    total, sizes, errors = measure(root)
    if not sizes and not errors and not (root / "examples" / "web" / "public" / "sw.js").is_file():
        return []
    out = list(errors)
    budget = max_bytes()
    if total > budget:
        detail = ", ".join(f"{name}={size}" for name, size in sizes)
        out.append(f"SW precache {total} bytes exceeds budget {budget} ({detail})")
    return out


def main() -> int:
    root = Path.cwd()
    errors = check(root)
    total, sizes, _ = measure(root)
    budget = max_bytes()
    for name, size in sizes:
        print(f"  {name}: {size} bytes")
    print(f"SW precache total: {total} bytes (budget: {budget})")
    if errors:
        print("\n".join(errors))
        return 1
    print("SW cache budget passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
