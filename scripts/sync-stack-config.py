#!/usr/bin/env python3
"""Propagate root app-update / donations config into stack exemplar paths."""
from __future__ import annotations

import json
import shutil
import sys
from pathlib import Path

WEB_PUBLIC = (
    "examples/web/public/app-update.json",
    "examples/web/public/donations.json",
)
ANDROID_ASSETS = (
    "examples/android/app/src/main/assets/app-update.json",
    "examples/android/app/src/main/assets/donations.json",
)


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def sync_app_update(root: Path, repo: str) -> None:
    if not repo.strip():
        return
    root_app = root / ".app-update.json"
    if root_app.is_file():
        data = json.loads(root_app.read_text(encoding="utf-8"))
    else:
        data = {
            "release_repo": repo.strip(),
            "check_interval": "weekly",
            "installed_artifact_format": "pwa",
            "restart_guard_key": "gp-update-restart-pending",
        }
    data["release_repo"] = repo.strip()

    web_path = root / WEB_PUBLIC[0]
    if web_path.parent.is_dir():
        web_data = {**data, "installed_artifact_format": "pwa"}
        write_json(web_path, web_data)

    android_path = root / ANDROID_ASSETS[0]
    if android_path.parent.is_dir():
        android_data = {
            "release_repo": repo.strip(),
            "installed_artifact_format": "apk",
        }
        write_json(android_path, android_data)


def sync_donations(root: Path, url: str) -> None:
    if not url.strip():
        return
    links = [{"label": "Donate", "url": url.strip()}]
    payload = {
        "enabled": True,
        "message": "If this project helps you, consider supporting development.",
        "links": links,
    }
    root_don = root / "donations.json"
    if root_don.is_file():
        payload = json.loads(root_don.read_text(encoding="utf-8"))
        if links:
            payload["links"] = links

    for rel in (WEB_PUBLIC[1], ANDROID_ASSETS[1]):
        path = root / rel
        if path.parent.is_dir():
            write_json(path, payload)


def seed_from_examples(root: Path) -> None:
    for example_rel, dest_rel in (
        (".app-update.json.example", ".app-update.json"),
        ("donations.json.example", "donations.json"),
    ):
        src = root / example_rel
        dst = root / dest_rel
        if src.is_file() and not dst.is_file():
            shutil.copy(src, dst)


def main() -> None:
    if len(sys.argv) != 4:
        print("Usage: sync-stack-config.py <root> <release_repo> <donation_url>", file=sys.stderr)
        sys.exit(1)
    root = Path(sys.argv[1])
    repo, url = sys.argv[2], sys.argv[3]
    seed_from_examples(root)
    sync_app_update(root, repo)
    sync_donations(root, url)


if __name__ == "__main__":
    main()
