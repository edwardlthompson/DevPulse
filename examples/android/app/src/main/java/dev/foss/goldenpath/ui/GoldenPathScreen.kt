package dev.foss.goldenpath.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.DonationsConfig
import dev.foss.goldenpath.ui.about.AboutScreen
import dev.foss.goldenpath.ui.components.GoldenPathScaffold
import dev.foss.goldenpath.ui.inventory.InventoryDetailScreen
import dev.foss.goldenpath.ui.inventory.InventoryScreen
import dev.foss.goldenpath.ui.inventory.InventoryUiModel
import dev.foss.goldenpath.ui.inventory.RefreshProgressBar
import dev.foss.goldenpath.ui.scan.ScanDetailScreen
import dev.foss.goldenpath.ui.scan.ScanScreen
import dev.foss.goldenpath.ui.scan.ScanSession
import dev.foss.goldenpath.ui.settings.SettingsScreen
import dev.foss.goldenpath.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldenPathScreen(
    snackbarHostState: SnackbarHostState,
    themeMode: ThemeMode,
    isOnline: Boolean,
    showAbout: Boolean,
    showSettings: Boolean,
    updateCheckEnabled: Boolean,
    appVersion: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    inventory: InventoryUiModel,
    scan: ScanSession,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onAboutOpen: () -> Unit,
    onAboutClose: () -> Unit,
    onSettingsOpen: () -> Unit,
    onSettingsClose: () -> Unit,
    onUpdateCheckChange: (Boolean) -> Unit,
    onApplyUpdate: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val overlayOpen = showSettings || showAbout || scan.visible || scan.selected != null ||
        inventory.selectedApp != null
    LaunchedEffect(inventory.selectedApp?.packageName) {
        if (inventory.selectedApp != null) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    BackHandler(enabled = imeVisible || overlayOpen) {
        if (imeVisible) {
            focusManager.clearFocus()
            keyboard?.hide()
            return@BackHandler
        }
        when {
            scan.selected != null -> scan.clearSelect()
            scan.visible -> scan.close()
            showAbout -> onAboutClose()
            showSettings -> onSettingsClose()
            inventory.selectedApp != null -> inventory.onClearSelect()
        }
    }
    GoldenPathScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    val onInventory = !showAbout && !showSettings && !scan.visible &&
                        scan.selected == null && inventory.selectedApp == null
                    if (onInventory && inventory.canScan) {
                        IconButton(onClick = inventory.onRefresh, enabled = !inventory.refreshing) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.inventory_refresh),
                            )
                        }
                        IconButton(onClick = inventory.onToggleSearch) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.inventory_search),
                            )
                        }
                        IconButton(onClick = inventory.onToggleFilters) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = stringResource(R.string.inventory_filter_open),
                            )
                        }
                    }
                    IconButton(onClick = onSettingsOpen) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_open),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (inventory.refreshing) {
                RefreshProgressBar(
                    done = inventory.refreshDone,
                    total = inventory.refreshTotal,
                    location = inventory.refreshLocation,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                InventoryScreen(model = inventory, modifier = Modifier.fillMaxSize())
                if (inventory.selectedApp != null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        InventoryDetailScreen(
                            app = inventory.selectedApp,
                            onBack = inventory.onClearSelect,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                if (showSettings) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        SettingsScreen(
                            themeMode = themeMode,
                            updateCheckEnabled = updateCheckEnabled,
                            onThemeModeSelect = onThemeModeSelect,
                            onUpdateCheckChange = onUpdateCheckChange,
                            onExport = inventory.onExport,
                            onAboutOpen = onAboutOpen,
                            onBack = onSettingsClose,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                if (showAbout) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        AboutScreen(
                            version = appVersion,
                            installedFormat = installedFormat,
                            updateStatus = updateStatus,
                            donations = donations,
                            canApplyUpdate = canApplyUpdate,
                            onApplyUpdate = onApplyUpdate,
                            onBack = onAboutClose,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                if (scan.visible && scan.selected == null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        ScanScreen(
                            items = scan.items,
                            progress = scan.progress,
                            onStart = scan.start,
                            onPause = scan.pause,
                            onResume = scan.resume,
                            onSelect = scan.select,
                            onClose = scan.close,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                if (scan.selected != null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        ScanDetailScreen(
                            detail = scan.selected,
                            onBack = scan.clearSelect,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
