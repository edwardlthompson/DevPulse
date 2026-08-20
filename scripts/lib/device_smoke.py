"""Device inventory smoke: honest dates + one direct APK download."""
from __future__ import annotations

import subprocess
import time
from datetime import datetime, timezone
from pathlib import Path

PKG = "app.devpulse"
ACTIVITY = "dev.foss.goldenpath.MainActivity"
STORE = "files/remote_releases.json"
EPOCH_1972_MS = int(datetime(1972, 1, 1, tzinfo=timezone.utc).timestamp() * 1000)


def adb_out(adb: str, args: list[str], timeout: int = 60) -> subprocess.CompletedProcess[str]:
    try:
        return subprocess.run([adb, *args], capture_output=True, text=True, check=False, timeout=timeout)
    except subprocess.TimeoutExpired:
        return subprocess.CompletedProcess(args=[adb, *args], returncode=1, stdout="", stderr="timeout")


def pull_store(adb: str) -> str:
    proc = adb_out(adb, ["exec-out", "run-as", PKG, "cat", STORE])
    return proc.stdout if proc.returncode == 0 else ""


def epoch_1970_rows(raw: str) -> list[str]:
    bad: list[str] = []
    for line in raw.splitlines():
        cols = line.split("\t")
        if len(cols) < 3 or not cols[2].strip():
            continue
        try:
            ms = int(cols[2])
        except ValueError:
            continue
        if 0 <= ms < EPOCH_1972_MS:
            bad.append(f"{cols[0]}\t{cols[1]}\t{ms}")
    return bad


def ui_has_1970(adb: str) -> bool:
    dump = Path.cwd() / ".cursor" / "device-ui.xml"
    dump.parent.mkdir(parents=True, exist_ok=True)
    adb_out(adb, ["shell", "uiautomator", "dump", "/sdcard/devpulse-ui.xml"])
    pulled = adb_out(adb, ["exec-out", "cat", "/sdcard/devpulse-ui.xml"])
    text = (pulled.stdout or "").lower()
    dump.write_text(pulled.stdout or "", encoding="utf-8")
    return "1970" in text or "1971" in text


def fdroid_apk_url(package_name: str) -> str | None:
    import json
    import urllib.request

    url = f"https://f-droid.org/api/v1/packages/{package_name}"
    try:
        with urllib.request.urlopen(url, timeout=20) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except Exception:
        return None
    code = data.get("suggestedVersionCode")
    if not code:
        return None
    return f"https://f-droid.org/repo/{package_name}_{code}.apk"


def start_download(adb: str, package_name: str, apk_url: str | None = None) -> None:
    args = ["shell", "am", "start", "-n", f"{PKG}/{ACTIVITY}", "--es", "download_package", package_name]
    if apk_url:
        args.extend(["--es", "download_url", apk_url])
    adb_out(adb, args)


def wait_log(adb: str, needle: str, seconds: int) -> bool:
    deadline = time.time() + seconds
    while time.time() < deadline:
        log = adb_out(adb, ["logcat", "-d", "-s", "DevPulse:I"], timeout=8)
        if log.returncode != 0:
            return False
        text = log.stdout or ""
        if needle in text:
            return True
        if "download identity failed" in text or "download failed " in text or "download skipped:" in text:
            if needle.startswith("download ready"):
                return False
        time.sleep(1)
    return False


def cached_apks(adb: str) -> list[str]:
    proc = adb_out(adb, ["exec-out", "run-as", PKG, "ls", "cache/updates"])
    if proc.returncode != 0:
        return []
    return [line.strip() for line in proc.stdout.splitlines() if line.strip().endswith(".apk")]


def pick_fdroid_packages(raw: str, limit: int = 8) -> list[str]:
    found: list[str] = []
    for line in raw.splitlines():
        cols = line.split("\t")
        if len(cols) >= 6 and cols[1] == "Fdroid" and cols[5] in {"1", "true"}:
            if cols[0] not in found:
                found.append(cols[0])
            if len(found) >= limit:
                break
    return found


def pick_fdroid_package(raw: str) -> str | None:
    packs = pick_fdroid_packages(raw, 1)
    return packs[0] if packs else None


def capture_jpeg(adb: str, dest: Path, max_width: int = 720) -> bool:
    raw = subprocess.run([adb, "exec-out", "screencap", "-p"], capture_output=True, check=False)
    if raw.returncode != 0 or not raw.stdout.startswith(b"\x89PNG"):
        return False
    dest.parent.mkdir(parents=True, exist_ok=True)
    png = dest.with_suffix(".png")
    png.write_bytes(raw.stdout)
    try:
        from PIL import Image
        img = Image.open(png)
        if img.width > max_width:
            height = int(img.height * max_width / img.width)
            img = img.resize((max_width, height))
        img.convert("RGB").save(dest, "JPEG", quality=70, optimize=True)
        png.unlink(missing_ok=True)
        return dest.is_file() and dest.stat().st_size <= 500_000
    except Exception:
        return png.is_file()


def play_only_package(raw: str) -> str | None:
    listed: dict[str, set[str]] = {}
    for line in raw.splitlines():
        cols = line.split("\t")
        if len(cols) < 6 or cols[5] not in {"1", "true"}:
            continue
        listed.setdefault(cols[0], set()).add(cols[1])
    for pkg, sources in listed.items():
        if "Play" in sources and "Fdroid" not in sources:
            return pkg
    return None
