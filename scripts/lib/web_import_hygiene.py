"""examples/web dependency hygiene — no CDN import maps; lockfile present."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def check(root: Path | None = None) -> list[str]:
    root = root or ROOT
    web = root / "examples" / "web"
    errors: list[str] = []
    if not web.is_dir():
        return []
    pkg = web / "package.json"
    if not pkg.is_file():
        return ["examples/web/package.json missing"]
    data = json.loads(pkg.read_text(encoding="utf-8"))
    if "imports" in data:
        errors.append("package.json must not define import maps (imports); use Vite bundled deps")
    index = web / "index.html"
    if index.is_file() and 'type="importmap"' in index.read_text(encoding="utf-8"):
        errors.append("index.html must not embed CDN import maps")
    if not (web / "package-lock.json").is_file():
        errors.append("examples/web/package-lock.json required")
    deps = {**data.get("dependencies", {}), **data.get("devDependencies", {})}
    for name in deps:
        if name.startswith("@example/") or name == "LEFTPAD":
            errors.append(f"suspicious dependency {name}")
    return errors


def main() -> int:
    errors = check()
    if errors:
        print("Web import/dependency hygiene failed:")
        for e in errors:
            print(f"  {e}")
        return 1
    print("examples/web import/dependency hygiene ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
