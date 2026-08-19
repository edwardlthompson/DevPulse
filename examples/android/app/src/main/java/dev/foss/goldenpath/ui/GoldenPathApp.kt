package dev.foss.goldenpath.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.BuildConfig
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.AppUpdatePreferences
import dev.foss.goldenpath.about.CheckSchedule
import dev.foss.goldenpath.about.DonationsLoader
import dev.foss.goldenpath.about.ReleaseAsset
import dev.foss.goldenpath.about.ReleaseAssetSelector
import dev.foss.goldenpath.about.ReleaseTagFetcher
import dev.foss.goldenpath.about.UpdateApplyCoordinator
import dev.foss.goldenpath.about.UpdateStatusEvaluator
import dev.foss.goldenpath.network.NetworkStatusMonitor
import dev.foss.goldenpath.settings.SettingsLogic
import androidx.compose.material3.SnackbarHostState
import dev.foss.goldenpath.ui.insets.NavigationModeProvider
import dev.foss.goldenpath.ui.theme.ThemeMode
import dev.foss.goldenpath.ui.theme.ThemePreferences
import dev.foss.goldenpath.ui.theme.next
import kotlinx.coroutines.CoroutineScope
import dev.foss.goldenpath.ui.theme.GoldenPathTheme
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
    val lastChecked by appUpdatePreferences.lastChecked.collectAsStateWithLifecycle(initialValue = null)
    val pendingRestart by appUpdatePreferences.pendingRestart.collectAsStateWithLifecycle(initialValue = false)
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf(context.getString(R.string.about_update_current)) }
    var applyAsset by remember { mutableStateOf<ReleaseAsset?>(null) }
    val donations = remember { DonationsLoader.load(context) }
    val appVersion = BuildConfig.VERSION_NAME
    val activity = context as? ComponentActivity

    LaunchedEffect(pendingRestart) {
        if (pendingRestart) {
            updateStatus = context.getString(R.string.about_update_restarting)
        }
    }

    LaunchedEffect(checkInterval, lastChecked, isOnline, installedFormat, pendingRestart) {
        if (pendingRestart) return@LaunchedEffect
        if (!isOnline) return@LaunchedEffect
        if (!CheckSchedule.shouldCheck(checkInterval, lastChecked, System.currentTimeMillis())) return@LaunchedEffect
        val repo = ReleaseTagFetcher.loadReleaseRepo(context) ?: return@LaunchedEffect
        val release = ReleaseTagFetcher.fetchLatestRelease(repo) ?: return@LaunchedEffect
        val format = installedFormat ?: "apk"
        if (release.assets.isNotEmpty() && ReleaseAssetSelector.select(release.assets, format) == null) {
            updateStatus = context.getString(R.string.about_update_no_compatible)
            return@LaunchedEffect
        }
        appUpdatePreferences.setLastChecked(System.currentTimeMillis())
        val selected = ReleaseAssetSelector.select(release.assets, format)
        applyAsset = when (val result = UpdateStatusEvaluator.evaluate(appVersion, release.tag)) {
            is UpdateStatusEvaluator.Result.Current -> {
                updateStatus = context.getString(R.string.about_update_current)
                null
            }
            is UpdateStatusEvaluator.Result.Available -> {
                updateStatus = context.getString(R.string.about_update_available, result.version)
                selected
            }
        }
    }

    val canApplyUpdate = applyAsset != null
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(canApplyUpdate) {
        if (canApplyUpdate) {
            snackbarHostState.showSnackbar(context.getString(R.string.snackbar_update_available))
        }
    }

    GoldenPathTheme(themeMode = themeMode) {
        NavigationModeProvider {
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
                canApplyUpdate = canApplyUpdate,
                onThemeToggle = { scope.launch { themePreferences.setThemeMode(themeMode.next()) } },
                onThemeModeSelect = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
                onAboutOpen = { showAbout = !showAbout; if (showAbout) showSettings = false },
                onAboutClose = { showAbout = false },
                onSettingsOpen = { showSettings = !showSettings; if (showSettings) showAbout = false },
                onSettingsClose = { showSettings = false },
                onUpdateCheckChange = { enabled ->
                    scope.launch {
                        appUpdatePreferences.setCheckInterval(
                            SettingsLogic.intervalForToggle(enabled, checkInterval),
                        )
                    }
                },
                onApplyUpdate = {
                    val asset = applyAsset ?: return@GoldenPathScreen
                    val host = activity ?: return@GoldenPathScreen
                    scope.launch {
                        UpdateApplyCoordinator.applySideloadUpdate(host, appUpdatePreferences, asset)
                    }
                },
            )
        }
    }
}
