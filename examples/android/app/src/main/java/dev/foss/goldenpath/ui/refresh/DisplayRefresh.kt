package dev.foss.goldenpath.ui.refresh

import android.app.Activity
import android.os.Build
import android.view.Display
import android.view.Window

data class RefreshMode(
    val modeId: Int,
    val width: Int,
    val height: Int,
    val refreshHz: Float,
)

object DisplayRefresh {
    fun pick(modes: List<RefreshMode>, current: RefreshMode): RefreshMode {
        if (modes.isEmpty() || current.refreshHz < 0f) return current
        val same = modes.filter { it.width == current.width && it.height == current.height }
        return (same.ifEmpty { modes }).maxBy { it.refreshHz }
    }

    fun from(display: Display): RefreshMode {
        val active = display.mode.toRefreshMode()
        return pick(display.supportedModes.map { it.toRefreshMode() }, active)
    }

    fun apply(activity: Activity) {
        val display = activity.displayOrNull() ?: return
        apply(activity.window, from(display))
    }

    fun apply(window: Window, choice: RefreshMode) {
        if (choice.refreshHz <= 0f) return
        val params = window.attributes
        params.preferredDisplayModeId = choice.modeId
        params.preferredRefreshRate = choice.refreshHz
        window.attributes = params
        if (Build.VERSION.SDK_INT >= 35) {
            window.setFrameRateBoostOnTouchEnabled(true)
            window.setFrameRatePowerSavingsBalanced(true)
            window.decorView.setRequestedFrameRate(choice.refreshHz)
        }
    }

    private fun Display.Mode.toRefreshMode() = RefreshMode(
        modeId,
        physicalWidth,
        physicalHeight,
        refreshRate,
    )

    private fun Activity.displayOrNull(): Display? {
        return if (Build.VERSION.SDK_INT >= 30) display else @Suppress("DEPRECATION") windowManager.defaultDisplay
    }
}
