package dev.foss.goldenpath.settings

object SettingsLogic {
    fun isUpdateCheckEnabled(interval: String): Boolean = interval != "off"

    fun intervalForToggle(enabled: Boolean, current: String): String =
        when {
            !enabled -> "off"
            current == "off" -> "weekly"
            else -> current
        }
}
