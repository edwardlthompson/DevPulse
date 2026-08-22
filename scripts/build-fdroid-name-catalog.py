#!/usr/bin/env python3
"""Build compact F-Droid / Izzy package-name catalogs for APK assets."""
from __future__ import annotations

import io
import json
import sys
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "examples/android/app/src/main/assets/fdroid-names"
OFFICIAL = "https://f-droid.org/repo/index-v1.jar"
IZZY = "https://apt.izzysoft.de/fdroid/repo/index-v1.jar"
UA = "DevPulse-catalog/0.1 (https://github.com/edwardlthompson/DevPulse)"


def fetch(url: str) -> bytes:
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=180) as resp:
        return resp.read()


def names_from_jar(raw: bytes) -> list[str]:
    with zipfile.ZipFile(io.BytesIO(raw)) as zf:
        member = next(n for n in zf.namelist() if n.endswith("index-v1.json"))
        data = json.loads(zf.read(member))
    found: set[str] = set()
    for app in data.get("apps") or []:
        if isinstance(app, dict):
            name = app.get("packageName")
            if isinstance(name, str):
                found.add(name)
    for key in data.get("packages") or {}:
        if isinstance(key, str):
            found.add(key)
    return sorted(n for n in found if n and "." in n)


def write_txt(path: Path, names: list[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(names) + "\n", encoding="utf-8")


def main() -> int:
    official = names_from_jar(fetch(OFFICIAL))
    izzy = names_from_jar(fetch(IZZY))
    write_txt(OUT / "official.txt", official)
    write_txt(OUT / "izzy.txt", izzy)
    meta = {"official": len(official), "izzy": len(izzy)}
    (OUT / "meta.json").write_text(json.dumps(meta) + "\n", encoding="utf-8")
    print(f"official={len(official)} izzy={len(izzy)} -> {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
