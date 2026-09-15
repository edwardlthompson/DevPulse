"""Skip Playwright browser install when the lockfile hash still matches."""
from __future__ import annotations

import hashlib
import sys
from pathlib import Path

STAMP_NAME = ".agent-bootstrap-playwright-hash"


def lock_hash(lock: Path) -> str:
    return hashlib.sha256(lock.read_bytes()).hexdigest()[:16]


def stamp_path(cache_dir: Path) -> Path:
    return cache_dir / STAMP_NAME


def should_skip_install(lock: Path, cache_dir: Path) -> bool:
    if not lock.is_file():
        return False
    stamp = stamp_path(cache_dir)
    if not stamp.is_file():
        return False
    try:
        return stamp.read_text(encoding="utf-8").strip() == lock_hash(lock)
    except OSError:
        return False


def mark_installed(lock: Path, cache_dir: Path) -> Path:
    cache_dir.mkdir(parents=True, exist_ok=True)
    path = stamp_path(cache_dir)
    path.write_text(lock_hash(lock) + "\n", encoding="utf-8")
    return path


def check_ci(text: str) -> list[str]:
    errors: list[str] = []
    if "hashFiles('examples/web/package-lock.json')" not in text:
        errors.append("CI must cache Playwright with hashFiles(package-lock.json)")
    if "playwright-cache.outputs.cache-hit" not in text:
        errors.append("CI must skip Playwright --with-deps on cache-hit")
    if "playwright install --with-deps" not in text:
        errors.append("CI must install Playwright --with-deps on cache miss")
    return errors


def main() -> int:
    root = Path.cwd()
    errors = check_ci((root / ".github" / "workflows" / "ci.yml").read_text(encoding="utf-8"))
    if errors:
        print("Playwright cache-hash check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Playwright cache-hash check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
