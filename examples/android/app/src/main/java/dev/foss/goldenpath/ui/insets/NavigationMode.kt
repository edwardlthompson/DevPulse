package dev.foss.goldenpath.ui.insets

import android.content.Context
import android.provider.Settings

enum class NavigationMode {
    ThreeButton,
    TwoButton,
    Gesture,
    Unknown,
}

fun Context.readNavigationMode(): NavigationMode {
    return try {
        when (Settings.Secure.getInt(contentResolver, NAVIGATION_MODE_KEY, -1)) {
            0 -> NavigationMode.ThreeButton
            1 -> NavigationMode.TwoButton
            2 -> NavigationMode.Gesture
            else -> NavigationMode.Unknown
        }
    } catch (_: SecurityException) {
        NavigationMode.Unknown
    } catch (_: Settings.SettingNotFoundException) {
        NavigationMode.Unknown
    }
}

private const val NAVIGATION_MODE_KEY = "navigation_mode"
