package dev.foss.goldenpath.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.SnackbarHostState
import dev.foss.goldenpath.BuildConfig
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.AppUpdatePreferences
import dev.foss.goldenpath.about.DonationsLoader
import dev.foss.goldenpath.network.NetworkStatusMonitor
import dev.foss.goldenpath.settings.SettingsLogic
import dev.foss.goldenpath.ui.about.ProductUpdateHost
import dev.foss.goldenpath.ui.insets.NavigationModeProvider
import dev.foss.goldenpath.ui.inventory.SignerReplaceHost
import dev.foss.goldenpath.ui.inventory.rememberInventoryUiModel
import dev.foss.goldenpath.ui.theme.GoldenPathTheme
import dev.foss.goldenpath.ui.theme.ThemeMode
import dev.foss.goldenpath.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun GoldenPathApp(
    context: Context,
    scope: CoroutineScope,
    themePreferences: ThemePreferences,
    appUpdatePreferences: AppUpdatePreferences,
    networkStatusMonitor: NetworkStatusMonitor,
) {
    val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System)
    val isOnline by networkStatusMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)
    val installedFormat by appUpdatePreferences.installedFormat.collectAsStateWithLifecycle(initialValue = "apk")
    val checkInterval by appUpdatePreferences.checkInterval.collectAsStateWithLifecycle(initialValue = "off")
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val updateStatus = context.getString(R.string.about_update_current)
    val donations = remember { DonationsLoader.load(context) }
    val appVersion = BuildConfig.VERSION_NAME
    val snackbarHostState = remember { SnackbarHostState() }
    val inventory = rememberInventoryUiModel(context, scope)

    GoldenPathTheme(themeMode = themeMode) {
        NavigationModeProvider {
            SignerReplaceHost()
            ProductUpdateHost(context = context, isOnline = isOnline, currentVersion = appVersion)
            GoldenPathScreen(
                snackbarHostState = snackbarHostState,
                themeMode = themeMode,
                isOnline = isOnline,
                showAbout = showAbout,
                showSettings = showSettings,
                updateCheckEnabled = SettingsLogic.isUpdateCheckEnabled(checkInterval),
                appVersion = appVersion,
                installedFormat = installedFormat ?: "apk",
                updateStatus = updateStatus,
                donations = donations,
                canApplyUpdate = false,
                inventory = inventory,
                onThemeModeSelect = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
                onAboutOpen = { showAbout = true },
                onAboutClose = { showAbout = false },
                onSettingsOpen = { showSettings = !showSettings },
                onSettingsClose = { showSettings = false },
                onUpdateCheckChange = { enabled ->
                    scope.launch {
                        appUpdatePreferences.setCheckInterval(
                            SettingsLogic.intervalForToggle(enabled, checkInterval),
                        )
                    }
                },
                onApplyUpdate = {},
            )
        }
    }
}
