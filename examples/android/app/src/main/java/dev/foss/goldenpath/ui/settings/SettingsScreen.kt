package dev.foss.goldenpath.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryExportFormat
import dev.foss.goldenpath.settings.SettingsPage
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    updateCheckEnabled: Boolean,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onExport: (InventoryExportFormat) -> Unit,
    onAboutOpen: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableStateOf<SettingsPage?>(null) }
    BackHandler(enabled = page != null) { page = null }
    when (page) {
        null -> SettingsHub(
            onOpenPage = { page = it },
            onAboutOpen = onAboutOpen,
            onClose = onBack,
            modifier = modifier,
        )
        SettingsPage.Permissions -> SettingsPane(
            title = stringResource(R.string.inventory_blocked),
            onBack = { page = null },
            modifier = modifier,
        ) { SettingsGroup { PermissionSettings() } }
        SettingsPage.Appearance -> SettingsPane(
            title = stringResource(R.string.settings_section_appearance),
            onBack = { page = null },
            modifier = modifier,
        ) { AppearanceSettings(themeMode, onThemeModeSelect) }
        SettingsPage.Inventory -> SettingsPane(
            title = stringResource(R.string.settings_section_inventory),
            onBack = { page = null },
            modifier = modifier,
        ) {
            SettingsGroup { IncludeSystemSettings() }
            SettingsGroup { ScanIntervalSettings() }
            SettingsGroup { StaleNotifySettings() }
            SettingsGroup { SettingsExportSection(onExport = onExport) }
        }
        SettingsPage.Updates -> SettingsPane(
            title = stringResource(R.string.settings_section_updates),
            onBack = { page = null },
            modifier = modifier,
        ) {
            SettingsGroup { UpdateCheckRow(updateCheckEnabled, onUpdateCheckChange) }
            SettingsGroup { InstallMethodSettings() }
            SettingsGroup { UpdatePrefetchSettings() }
        }
        SettingsPage.Sources -> SettingsPane(
            title = stringResource(R.string.settings_section_sources),
            onBack = { page = null },
            modifier = modifier,
        ) {
            SettingsGroup { PlayLookupSettings() }
            SettingsGroup { FdroidRepoSettings() }
            SettingsGroup { AptoideLookupSettings() }
            SettingsGroup { DumpStoreLookupSettings() }
            SettingsGroup { ForgeLookupSettings() }
            SettingsGroup { ForgePasteSettings() }
        }
        SettingsPage.Stores -> SettingsPane(
            title = stringResource(R.string.settings_section_stores),
            onBack = { page = null },
            modifier = modifier,
        ) { StoreClientSettings(showTitle = false) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceSettings(themeMode: ThemeMode, onThemeModeSelect: (ThemeMode) -> Unit) {
    SettingsGroup {
        Text(text = stringResource(R.string.settings_theme_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeSelect(mode) },
                    label = {
                        Text(
                            when (mode) {
                                ThemeMode.System -> stringResource(R.string.settings_theme_mode_system)
                                ThemeMode.Light -> stringResource(R.string.settings_theme_mode_light)
                                ThemeMode.Dark -> stringResource(R.string.settings_theme_mode_dark)
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun UpdateCheckRow(enabled: Boolean, onChange: (Boolean) -> Unit) {
    val label = stringResource(R.string.settings_update_check_label)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}
