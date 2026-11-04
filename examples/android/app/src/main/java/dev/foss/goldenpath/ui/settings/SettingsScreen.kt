package dev.foss.goldenpath.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryExportFormat
import dev.foss.goldenpath.settings.SettingsPage
import dev.foss.goldenpath.ui.components.MenuOverlay
import dev.foss.goldenpath.ui.opportunity.OpportunityPane
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
    apps: List<InstalledApp> = emptyList(),
) {
    var page by remember { mutableStateOf<SettingsPage?>(null) }
    val childOpen = page != null
    BackHandler(enabled = childOpen) { page = null }
    MenuOverlay(
        open = childOpen,
        modifier = modifier,
        parent = {
            SettingsHub(
                onOpenPage = { page = it },
                onAboutOpen = onAboutOpen,
                modifier = Modifier.fillMaxSize(),
            )
        },
        child = {
            when (val open = page) {
                null -> Unit
                SettingsPage.Permissions -> SettingsPane(
                    title = stringResource(R.string.inventory_blocked),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) { SettingsGroup { PermissionSettings() } }
                SettingsPage.Appearance -> SettingsPane(
                    title = stringResource(R.string.settings_section_appearance),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) { AppearanceSettings(themeMode, onThemeModeSelect) }
                SettingsPage.Privacy -> SettingsPane(
                    title = stringResource(R.string.settings_section_privacy),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) { SettingsGroup { PrivacySettings() } }
                SettingsPage.Inventory -> SettingsPane(
                    title = stringResource(R.string.settings_section_inventory),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SettingsGroup { IncludeSystemSettings() }
                    SettingsGroup { ScanIntervalSettings() }
                    SettingsGroup { StaleNotifySettings() }
                    SettingsGroup { SettingsExportSection(onExport = onExport) }
                }
                SettingsPage.Ideas -> SettingsPane(
                    title = stringResource(R.string.opportunity_title),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    OpportunityPane(
                        apps = apps,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                SettingsPage.History -> SettingsPane(
                    title = stringResource(R.string.settings_section_history),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) { HistorySettings() }
                SettingsPage.Updates -> SettingsPane(
                    title = stringResource(R.string.settings_section_updates),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SettingsGroup { UpdateCheckRow(updateCheckEnabled, onUpdateCheckChange) }
                    SettingsGroup { InstallMethodSettings() }
                    SettingsGroup { UpdatePrefetchSettings() }
                }
                SettingsPage.Sources -> SourcesScreen(
                    onBack = { page = null },
                    apps = apps,
                    modifier = Modifier.fillMaxSize(),
                )
                SettingsPage.Stores -> SettingsPane(
                    title = stringResource(R.string.settings_section_stores),
                    onBack = { page = null },
                    modifier = Modifier.fillMaxSize(),
                ) { StoreClientSettings(showTitle = false) }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSettings(themeMode: ThemeMode, onThemeModeSelect: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (themeMode) {
        ThemeMode.System -> stringResource(R.string.settings_theme_mode_system)
        ThemeMode.Light -> stringResource(R.string.settings_theme_mode_light)
        ThemeMode.Dark -> stringResource(R.string.settings_theme_mode_dark)
    }
    SettingsGroup {
        Text(text = stringResource(R.string.settings_theme_label))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ThemeMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (mode) {
                                    ThemeMode.System -> stringResource(R.string.settings_theme_mode_system)
                                    ThemeMode.Light -> stringResource(R.string.settings_theme_mode_light)
                                    ThemeMode.Dark -> stringResource(R.string.settings_theme_mode_dark)
                                },
                            )
                        },
                        onClick = {
                            onThemeModeSelect(mode)
                            expanded = false
                        },
                    )
                }
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
