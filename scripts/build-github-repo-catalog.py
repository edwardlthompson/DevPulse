#!/usr/bin/env python3
"""Build a shipped package→GitHub owner/repo TSV from F-Droid indexes."""
from __future__ import annotations

import io
import json
import sys
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "examples/android/app/src/main/assets/github-repos"
OFFICIAL = "https://f-droid.org/repo/index-v1.jar"
IZZY = "https://apt.izzysoft.de/fdroid/repo/index-v1.jar"
UA = "DevPulse-catalog/0.1 (https://github.com/edwardlthompson/DevPulse)"
RESERVED = {
    "about",
    "apps",
    "features",
    "login",
    "marketplace",
    "orgs",
    "settings",
    "signup",
    "sponsors",
    "topics",
}


def fetch(url: str) -> bytes:
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=180) as resp:
        return resp.read()


def owner_repo(raw: object) -> str | None:
    if not isinstance(raw, str):
        return None
    url = raw.strip()
    if not url.startswith("http://") and not url.startswith("https://"):
        return None
    host_path = url.removeprefix("https://").removeprefix("http://")
    if host_path.startswith("www."):
        host_path = host_path[4:]
    slash = host_path.find("/")
    if slash <= 0:
        return None
    host = host_path[:slash].lower()
    if host != "github.com":
        return None
    rest = host_path[slash + 1 :]
    parts = [p for p in rest.replace("?", "/").replace("#", "/").split("/") if p]
    if len(parts) < 2:
        return None
    owner = parts[0]
    repo = parts[1].removesuffix(".git")
    if owner in RESERVED or repo in RESERVED:
        return None
    if "/" in owner or not owner or not repo:
        return None
    return f"{owner}/{repo}"


def harvest_jar(raw: bytes) -> dict[str, str]:
    with zipfile.ZipFile(io.BytesIO(raw)) as zf:
        member = next(n for n in zf.namelist() if n.endswith("index-v1.json"))
        data = json.loads(zf.read(member))
    found: dict[str, str] = {}
    for app in data.get("apps") or []:
        if not isinstance(app, dict):
            continue
        pkg = app.get("packageName")
        if not isinstance(pkg, str) or "." not in pkg:
            continue
        mapped = owner_repo(app.get("sourceCode"))
        if mapped:
            found[pkg] = mapped
    return found


def write_tsv(path: Path, rows: dict[str, str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = [f"{pkg}\t{repo}\n" for pkg, repo in sorted(rows.items())]
    path.write_text("".join(lines), encoding="utf-8")


def main() -> int:
    official = harvest_jar(fetch(OFFICIAL))
    izzy = harvest_jar(fetch(IZZY))
    merged = dict(izzy)
    merged.update(official)
    write_tsv(OUT / "verified.tsv", merged)
    meta = {"rows": len(merged), "official": len(official), "izzy": len(izzy)}
    (OUT / "meta.json").write_text(json.dumps(meta) + "\n", encoding="utf-8")
    print(f"rows={len(merged)} official={len(official)} izzy={len(izzy)} -> {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
