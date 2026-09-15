"""DevPulse HUMAN/ADB automation: source contracts, unit tests, optional device UI."""
from __future__ import annotations

import os
from pathlib import Path

from human_task_adb_device import run_connected_filter
from human_task_android import _gradle_argv, adb_authorized
from human_task_core import AttemptResult, run_cmd

HONEST = [
    "examples/android/app/src/main/res/values/inventory-ui.xml",
    "examples/android/app/src/main/res/values/update-all.xml",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/GoldenPathScreen.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/UpdateAllDialog.kt",
]
PULSE = [
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/InventoryRow.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/InventoryScreen.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/AppListScroller.kt",
]
DETAIL = [
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/InventoryDetailScreen.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/inventory/InventoryDetailAdvanced.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/inventory/InventoryDetailChrome.kt",
]
PRIVACY = [
    "examples/android/app/src/main/java/dev/foss/goldenpath/crashcapture/CrashCapture.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/notify/UnifiedPushGate.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/feedback/FeedbackGithub.kt",
    "examples/android/app/src/main/java/dev/foss/goldenpath/ui/settings/PrivacySettings.kt",
]


def _blob(root: Path, rels: list[str]) -> str:
    parts: list[str] = []
    for rel in rels:
        path = root / rel
        if path.is_file():
            parts.append(path.read_text(encoding="utf-8"))
    return "\n".join(parts)


def _need(blob: str, needles: tuple[str, ...]) -> str | None:
    for needle in needles:
        if needle not in blob:
            return f"missing {needle}"
    return None


def _units(root: Path, tests: tuple[str, ...]) -> AttemptResult | None:
    sdk = Path.home() / "Android" / "Sdk"
    if sdk.is_dir():
        os.environ.setdefault("ANDROID_HOME", str(sdk))
        os.environ.setdefault("ANDROID_SDK_ROOT", str(sdk))
        props = root / "examples/android/local.properties"
        if not props.is_file():
            props.write_text(f"sdk.dir={sdk}\n", encoding="utf-8")
    args: list[str] = ["testDebugUnitTest"]
    for name in tests:
        args.extend(["--tests", name])
    argv = _gradle_argv(root, *args)
    if not argv:
        return AttemptResult(1, "gradle", "examples/android gradlew missing", True)
    code, tail = run_cmd(root, argv, cwd=root / "examples/android")
    if code != 0:
        return AttemptResult(1, "unit", tail or f"exit {code}", True)
    return None


def _device(root: Path) -> AttemptResult | None:
    if not adb_authorized(root):
        return None
    stamp = Path("/tmp/devpulse-goldenpath-ui-ok")
    if stamp.is_file():
        return AttemptResult(0, "device-cached", "GoldenPathUiTest already passed", False)
    result = run_connected_filter(root, "dev.foss.goldenpath.GoldenPathUiTest")
    if result.exit_code == 0:
        stamp.write_text("ok", encoding="utf-8")
    return result


def _finish(root: Path, method: str, ok: str) -> AttemptResult:
    extra = _device(root)
    if extra is not None and extra.exit_code != 0:
        return extra
    note = ok if extra is None else f"{ok}; GoldenPathUiTest on device"
    return AttemptResult(0, method, note, False)


def automate_devpulse_honesty(root: Path, _cfg: dict) -> AttemptResult:
    miss = _need(
        _blob(root, HONEST),
        ("install_fail_no_file", "listing_abi_mismatch", "update_all_hide", "LinearProgressIndicator", "onHide"),
    )
    if miss:
        return AttemptResult(1, "honesty", miss, True)
    fail = _units(root, ("dev.foss.goldenpath.inventory.InventoryEmptyKindTest", "dev.foss.goldenpath.inventory.InventoryCopyTest"))
    return fail or _finish(root, "honesty", "empty/fail/Hide-Stop/ABI copy locked")


def automate_devpulse_pulse(root: Path, _cfg: dict) -> AttemptResult:
    miss = _need(_blob(root, PULSE), ("CircleShape", "ModalBottomSheet", "TRANSITION_ANIMATION_SCALE"))
    if miss:
        return AttemptResult(1, "pulse", miss, True)
    fail = _units(root, ("dev.foss.goldenpath.inventory.InventoryCopyTest",))
    return fail or _finish(root, "pulse", "pulse/sheet/reduce-motion locked")


def automate_devpulse_detail(root: Path, _cfg: dict) -> AttemptResult:
    miss = _need(_blob(root, DETAIL), ("inventory_advanced", "DetailPasteRepo", "DetailGithubOpts"))
    if miss:
        return AttemptResult(1, "detail", miss, True)
    fail = _units(root, ("dev.foss.goldenpath.inventory.InventoryDetailChromeTest",))
    return fail or _finish(root, "detail", "Advanced paste/regex locked")


def automate_devpulse_optin(root: Path, _cfg: dict) -> AttemptResult:
    blob = _blob(root, PRIVACY)
    miss = _need(blob, ("enabled: Boolean = false", "const val enabled: Boolean = false", "uriHandler.openUri"))
    if miss:
        return AttemptResult(1, "opt-in", miss, True)
    privacy_ui = _blob(root, [PRIVACY[3]])
    if "LaunchedEffect" in privacy_ui:
        return AttemptResult(1, "opt-in", "PrivacySettings must not auto-open GitHub", True)
    fail = _units(
        root,
        (
            "dev.foss.goldenpath.crashcapture.PendingCrashTest",
            "dev.foss.goldenpath.notify.UnifiedPushGateTest",
            "dev.foss.goldenpath.feedback.FeedbackComposeTest",
        ),
    )
    return fail or _finish(root, "opt-in", "capture/UnifiedPush off; GitHub only on tap")


def automate_devpulse_skip_nav(root: Path, _cfg: dict) -> AttemptResult:
    chrome = _blob(root, ["examples/android/app/src/main/java/dev/foss/goldenpath/ui/NavigationChrome.kt"])
    if "bottomNav: Boolean = false" not in chrome:
        return AttemptResult(1, "skip-nav", "bottom nav still enabled", True)
    fail = _units(root, ("dev.foss.goldenpath.ui.NavigationChromeTest",))
    return fail or AttemptResult(0, "skip-nav", "home calm; bottom nav skipped", False)
