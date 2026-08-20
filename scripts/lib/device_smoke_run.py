"""Run on-device date and APK-download smokes."""
from __future__ import annotations

from device_smoke import (
    ACTIVITY,
    PKG,
    adb_out,
    cached_apks,
    epoch_1970_rows,
    fdroid_apk_url,
    pick_fdroid_packages,
    play_only_package,
    pull_store,
    start_download,
    ui_has_1970,
    wait_log,
)


def run_date_smoke(adb: str) -> str | None:
    raw = pull_store(adb)
    if not raw.strip():
        return "remote_releases.json missing on device"
    bad = epoch_1970_rows(raw)
    if bad:
        return f"epoch 1970/1971 rows: {bad[:3]}"
    adb_out(adb, ["shell", "am", "start", "-n", f"{PKG}/{ACTIVITY}"])
    if ui_has_1970(adb):
        return "UI dump contains 1970 or 1971"
    return None


def run_download_smoke(adb: str) -> str | None:
    raw = pull_store(adb)
    packs = pick_fdroid_packages(raw, 3)
    if not packs:
        return "no F-Droid listed package in store"
    adb_out(adb, ["logcat", "-c"])
    hit = None
    for pkg in packs:
        start_download(adb, pkg, fdroid_apk_url(pkg))
        if (
            wait_log(adb, f"download ready {pkg}", 90)
            or wait_log(adb, f"download identity failed {pkg}", 5)
            or cached_apks(adb)
        ):
            hit = pkg
            break
    play = play_only_package(raw)
    if play:
        start_download(adb, play)
        wait_log(adb, "download skipped: no file url", 30)
    if not hit:
        return f"download did not cache {packs[0]}"
    return None
