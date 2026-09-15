package dev.foss.goldenpath.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.DonationsConfig
import dev.foss.goldenpath.inventory.UpdateAllSession
import dev.foss.goldenpath.ui.about.AboutScreen
import dev.foss.goldenpath.ui.components.GoldenPathScaffold
import dev.foss.goldenpath.ui.components.MenuOverlay
import dev.foss.goldenpath.ui.forge.AddRepoDialog
import dev.foss.goldenpath.ui.inventory.InventoryDetailScreen
import dev.foss.goldenpath.ui.inventory.InventoryScreen
import dev.foss.goldenpath.ui.inventory.InventoryUiModel
import dev.foss.goldenpath.ui.inventory.PulseRunHost
import dev.foss.goldenpath.ui.settings.SettingsScreen
import dev.foss.goldenpath.ui.theme.ThemeMode
import kotlinx.coroutines.launch

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
    var showAddRepo by remember { mutableStateOf(false) }
    var longPressHinted by remember { mutableStateOf(false) }
    var scanUpdateKick by remember { mutableIntStateOf(0) }
    val longPressHint = stringResource(R.string.inventory_longpress_hint)
    val scope = rememberCoroutineScope()
    val lookupDone = inventory.refreshTotal > 0 && inventory.refreshDone >= inventory.refreshTotal
    val refreshDismissible = inventory.showRefreshDialog
    val overlayOpen = showSettings || showAbout || inventory.selectedApp != null
    val onInventory = !showAbout && !showSettings && inventory.selectedApp == null
    LaunchedEffect(inventory.selectedApp?.packageName) {
        if (inventory.selectedApp != null) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    BackHandler(enabled = imeVisible || overlayOpen || refreshDismissible || showAddRepo) {
        if (imeVisible) {
            focusManager.clearFocus()
            keyboard?.hide()
            return@BackHandler
        }
        when {
            refreshDismissible -> {
                inventory.onDismissRefresh()
                UpdateAllSession.hide()
            }
            showAddRepo -> showAddRepo = false
            showAbout -> onAboutClose()
            showSettings -> onSettingsClose()
            inventory.selectedApp != null -> inventory.onClearSelect()
        }
    }
    GoldenPathScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            when {
                                showAbout -> R.string.about_title
                                showSettings -> R.string.settings_title
                                else -> R.string.app_title
                            },
                        ),
                    )
                },
                actions = {
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
                    IconButton(onClick = if (showSettings) onSettingsClose else onSettingsOpen) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(
                                if (showSettings) R.string.settings_close else R.string.settings_open,
                            ),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
                MenuOverlay(
                    open = inventory.selectedApp != null,
                    modifier = Modifier.fillMaxSize(),
                    parent = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            InventoryScreen(
                                model = inventory,
                                onUpdateSelectHint = {
                                    if (!longPressHinted) {
                                        longPressHinted = true
                                        scope.launch { snackbarHostState.showSnackbar(longPressHint) }
                                    }
                                },
                                scanUpdateKick = scanUpdateKick,
                                modifier = Modifier.fillMaxSize(),
                            )
                            if (inventory.refreshing && !inventory.showRefreshDialog) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .fillMaxWidth()
                                        .height(2.dp),
                                )
                            }
                        }
                    },
                    child = {
                        when (val app = inventory.selectedApp) {
                            null -> Unit
                            else -> InventoryDetailScreen(
                                app = app,
                                onBack = inventory.onClearSelect,
                                inventory = inventory.apps,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                )
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
                            apps = inventory.apps,
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
        }
    }
    PulseRunHost(
        inventory = inventory,
        lookupDone = lookupDone,
        onKickUpdate = { scanUpdateKick++ },
    )
    if (showAddRepo) {
        AddRepoDialog(
            installed = inventory.apps,
            onDismiss = { showAddRepo = false },
            onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
        )
    }
}
