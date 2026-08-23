package dev.foss.goldenpath.inventory

import android.content.Context
import android.provider.Settings

object AirplaneMode {
    fun on(context: Context): Boolean =
        runCatching { Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1 }
            .getOrDefault(false)
}
