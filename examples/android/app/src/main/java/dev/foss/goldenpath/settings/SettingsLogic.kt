package dev.foss.goldenpath.settings

import dev.foss.goldenpath.R

enum class SettingsPage { Appearance, Inventory, History, Updates, Sources, Stores, Permissions }

data class SettingsHubRow(
    val page: SettingsPage?,
    val titleRes: Int,
    val summaryRes: Int,
)

object SettingsNav {
    fun hubRows(): List<SettingsHubRow> = listOf(
        SettingsHubRow(SettingsPage.Appearance, R.string.settings_section_appearance, R.string.settings_hub_appearance),
        SettingsHubRow(SettingsPage.Permissions, R.string.inventory_blocked, R.string.inventory_usage_seen),
        SettingsHubRow(SettingsPage.Inventory, R.string.settings_section_inventory, R.string.settings_hub_inventory),
        SettingsHubRow(SettingsPage.History, R.string.settings_section_history, R.string.settings_hub_history),
        SettingsHubRow(SettingsPage.Updates, R.string.settings_section_updates, R.string.settings_hub_updates),
        SettingsHubRow(SettingsPage.Sources, R.string.settings_section_sources, R.string.settings_hub_sources),
        SettingsHubRow(SettingsPage.Stores, R.string.settings_section_stores, R.string.settings_hub_stores),
        SettingsHubRow(null, R.string.about_title, R.string.settings_hub_about),
    )
}

object MenuPlace {
    fun composeParent(exclusiveSwap: Boolean, childOpen: Boolean): Boolean =
        if (exclusiveSwap) !childOpen else true
}

object SettingsLogic {
    fun isUpdateCheckEnabled(interval: String): Boolean = interval != "off"

    fun intervalForToggle(enabled: Boolean, current: String): String =
        when {
            !enabled -> "off"
            current == "off" -> "weekly"
            else -> current
        }
}
