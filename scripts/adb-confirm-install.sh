#!/usr/bin/env bash
# Tap PackageInstaller Update / Install / Done so Update all is not blocked on confirms.
# Usage: bash scripts/adb-confirm-install.sh [serial]
set -euo pipefail
export ANDROID_SERIAL="${1:-${ANDROID_SERIAL:-}}"
exec python3 - <<'PY'
import os, re, subprocess, time

ser = os.environ.get("ANDROID_SERIAL") or ""
adb = ["adb"] + (["-s", ser] if ser else [])
dump = "/sdcard/uidump.xml"
want = {"update", "install", "done"}
taps = 0
idle = 0
while taps < 80 and idle < 40:
    subprocess.run(adb + ["shell", "uiautomator", "dump", dump], capture_output=True, timeout=20)
    pulled = subprocess.run(adb + ["shell", "cat", dump], capture_output=True, text=True, timeout=20)
    xml = pulled.stdout or ""
    hits = []
    for m in re.finditer(r"<node[^>]+>", xml):
        n = m.group(0)
        text = re.search(r'text="([^"]*)"', n)
        click = re.search(r'clickable="true"', n)
        bounds = re.search(r"bounds=\"\[(\d+),(\d+)\]\[(\d+),(\d+)\]\"", n)
        if not (click and text and bounds):
            continue
        label = text.group(1).strip()
        if label.lower() not in want:
            continue
        x1, y1, x2, y2 = map(int, bounds.groups())
        hits.append((label, (x1 + x2) // 2, (y1 + y2) // 2))
    if not hits:
        idle += 1
        time.sleep(1)
        continue
    label, x, y = hits[0]
    print(f"tap {taps + 1} {label} {x},{y}", flush=True)
    subprocess.run(adb + ["shell", "input", "tap", str(x), str(y)], capture_output=True)
    taps += 1
    idle = 0
    time.sleep(1)
print(f"taps={taps} idle={idle}")
PY
