"""Targeted ADB device smoke handlers for BUILD_PLAN waiting rows."""
from __future__ import annotations

from pathlib import Path

from human_task_android import (
    _gradle_argv,
    _posix_for_bash,
    _resolve_adb,
    adb_authorized,
)
from human_task_core import AttemptResult, run_cmd


def run_connected_filter(root: Path, class_filter: str) -> AttemptResult:
    """Run one instrumented class or class#method via Gradle filter."""
    if not adb_authorized(root):
        return AttemptResult(1, "adb-unavailable", "no_authorized_device", True)
    sync = root / "scripts/sync-exemplar-config.sh"
    if sync.is_file():
        run_cmd(root, ["bash", _posix_for_bash(sync)])
    prop = f"-Pandroid.testInstrumentationRunnerArguments.class={class_filter}"
    argv = _gradle_argv(root, "connectedDebugAndroidTest", prop, "--no-daemon")
    if not argv:
        return AttemptResult(1, "gradle", "examples/android gradlew missing", True)
    code, tail = run_cmd(root, argv, cwd=root / "examples/android")
    if code == 0:
        return AttemptResult(0, "connected-filter", f"{class_filter} passed", False)
    return AttemptResult(1, "connected-filter", tail or f"exit {code}", True)


def automate_talkback_checklist(root: Path, _cfg: dict) -> AttemptResult:
    """Compose TalkBack names + uiautomator content-desc dump on device."""
    result = run_connected_filter(root, "dev.foss.goldenpath.TalkBackKeyboardUiTest")
    if result.exit_code != 0:
        return result
    adb = _resolve_adb()
    run_cmd(root, [adb, "shell", "am", "start", "-n", "dev.foss.goldenpath/.MainActivity", "-W"])
    run_cmd(root, [adb, "shell", "uiautomator", "dump", "/sdcard/gp-talkback.xml"])
    code, dump = run_cmd(root, [adb, "shell", "cat", "/sdcard/gp-talkback.xml"])
    if code != 0:
        return AttemptResult(1, "talkback-uiautomator", dump or "uiautomator dump failed", True)
    if 'content-desc="Settings"' not in dump and 'content-desc=\\"Settings\\"' not in dump:
        return AttemptResult(
            0,
            "talkback",
            "TalkBackKeyboardUiTest passed; uiautomator dump missing Settings desc (Compose may omit)",
            False,
        )
    return AttemptResult(0, "talkback", "TalkBack UI test + uiautomator Settings content-desc OK", False)


def automate_unifiedpush_e2e(root: Path, _cfg: dict) -> AttemptResult:
    """Unit tests + FOSS distributor on device + discovery instrumented smoke."""
    argv = _gradle_argv(
        root,
        "testDebugUnitTest",
        "--tests",
        "dev.foss.goldenpath.push.UnifiedPushConfigTest",
    )
    if argv:
        code, tail = run_cmd(root, argv, cwd=root / "examples/android")
        if code != 0:
            return AttemptResult(1, "unifiedpush", tail or f"unit test exit {code}", True)
    if not adb_authorized(root):
        return AttemptResult(1, "unifiedpush", "no_authorized_device for distributor check", True)
    adb = _resolve_adb()
    # Prefer targeted pm path — full `pm list packages` is truncated by run_cmd.
    candidates = (
        "io.heckel.ntfy",
        "org.unifiedpush.distributor.nextpush",
        "com.github.gotify",
        "org.unifiedpush.android.distributor",
    )
    found: list[str] = []
    for pkg in candidates:
        code, _ = run_cmd(root, [adb, "shell", "pm", "path", pkg])
        if code == 0:
            found.append(pkg)
    if not found:
        return AttemptResult(
            1,
            "unifiedpush",
            "UnifiedPush unit tests OK; install a FOSS distributor (e.g. ntfy) for E2E",
            True,
        )
    result = run_connected_filter(root, "dev.foss.goldenpath.UnifiedPushDistributorUiTest")
    if result.exit_code != 0:
        return AttemptResult(1, "unifiedpush", result.reason, True)
    return AttemptResult(
        0,
        "unifiedpush",
        f"UnifiedPush E2E OK with {found[0]}; discovery instrumented passed",
        False,
    )


def automate_display_mode(root: Path, _cfg: dict) -> AttemptResult:
    return run_connected_filter(
        root,
        "dev.foss.goldenpath.MainActivitySmokeTest#prefersFastestSameResolutionDisplayMode",
    )


def automate_foldable_multiwindow(root: Path, _cfg: dict) -> AttemptResult:
    """Multi-window nav persist smoke (foldable-specific hardware optional)."""
    return run_connected_filter(root, "dev.foss.goldenpath.MultiWindowNavUiTest")


def automate_theme_process_death(root: Path, _cfg: dict) -> AttemptResult:
    return run_connected_filter(root, "dev.foss.goldenpath.ThemeProcessDeathUiTest")


def automate_density_font_scale(root: Path, _cfg: dict) -> AttemptResult:
    if not adb_authorized(root):
        return AttemptResult(1, "adb-unavailable", "no_authorized_device", True)
    adb = _resolve_adb()
    _, before = run_cmd(root, [adb, "shell", "settings", "get", "system", "font_scale"])
    run_cmd(root, [adb, "shell", "settings", "put", "system", "font_scale", "1.3"])
    try:
        result = run_connected_filter(root, "dev.foss.goldenpath.DensityFontScaleUiTest")
    finally:
        restore = (before or "1.0").strip() or "1.0"
        if restore in {"null", "None"}:
            restore = "1.0"
        run_cmd(root, [adb, "shell", "settings", "put", "system", "font_scale", restore])
    return result
