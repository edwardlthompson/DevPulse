package dev.foss.goldenpath.ui.inventory

import android.Manifest
import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.inventory.InventoryExportFormat
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.InventoryPresent
import dev.foss.goldenpath.inventory.InventorySortMode
import dev.foss.goldenpath.inventory.InventorySourceFilter
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.PackageManagerPackageCatalog
import dev.foss.goldenpath.inventory.QueryAllPackagesGate
import dev.foss.goldenpath.inventory.FileRemoteReleaseStore
import dev.foss.goldenpath.inventory.RefreshProgress
import dev.foss.goldenpath.inventory.ReleaseRefreshRuntime
import dev.foss.goldenpath.inventory.ReleaseRefreshService
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import java.io.File
import dev.foss.goldenpath.inventory.ScanInterval
import dev.foss.goldenpath.inventory.ScanSchedule
import dev.foss.goldenpath.inventory.UsagePulse
import dev.foss.goldenpath.inventory.UsageStatsAccess
import dev.foss.goldenpath.inventory.UsageStatsConsent
import dev.foss.goldenpath.inventory.UsageStatsManagerCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InventoryUiModel(
    val apps: List<InstalledApp>,
    val canScan: Boolean,
    val rationaleSkipped: Boolean,
    val showUsageWalkthrough: Boolean,
    val query: String,
    val sortMode: InventorySortMode,
    val staleOnly: Boolean,
    val updatesOnly: Boolean,
    val sourceFilters: Set<RemoteReleasedSource>,
    val selectedApp: InstalledApp?,
    val canRankByUsage: Boolean,
    val showSearch: Boolean,
    val showFilters: Boolean,
    val refreshing: Boolean,
    val refreshDone: Int,
    val refreshTotal: Int,
    val refreshLocation: String,
    val onAcknowledge: () -> Unit,
    val onSkip: () -> Unit,
    val onDismissUsage: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onSortMode: (InventorySortMode) -> Unit,
    val onStaleOnlyChange: (Boolean) -> Unit,
    val onUpdatesOnlyChange: (Boolean) -> Unit,
    val onToggleSourceFilter: (RemoteReleasedSource) -> Unit,
    val onExport: (InventoryExportFormat) -> Unit,
    val onSelect: (String) -> Unit,
    val onClearSelect: () -> Unit,
    val onOpenUsageAccess: () -> Unit,
    val onToggleSearch: () -> Unit,
    val onToggleFilters: () -> Unit,
    val onRefresh: () -> Unit,
)

@Composable
fun rememberInventoryUiModel(context: Context, scope: CoroutineScope): InventoryUiModel {
    val prefs = remember { InventoryPreferences(context) }
    val catalog = remember { PackageManagerPackageCatalog(context.packageManager) }
    remember {
        RemoteReleaseMemory.hydrate(FileRemoteReleaseStore(File(context.filesDir, "remote_releases.json")))
    }
    val acknowledged by prefs.queryAllPackagesAcknowledged.collectAsStateWithLifecycle(false)
    val includeSystem by prefs.includeSystemApps.collectAsStateWithLifecycle(false)
    val consent by prefs.usageStatsConsent.collectAsStateWithLifecycle(UsageStatsConsent.NotOffered)
    val sortMode by prefs.sortMode.collectAsStateWithLifecycle(InventorySortMode.Oldest)
    val staleOnly by prefs.staleOnly.collectAsStateWithLifecycle(false)
    val updatesOnly by prefs.updatesOnly.collectAsStateWithLifecycle(false)
    val sourceFilters by prefs.sourceFilters.collectAsStateWithLifecycle(emptySet())
    val revision by RemoteReleaseMemory.revision.collectAsStateWithLifecycle(0)
    val refreshing by ReleaseRefreshRuntime.running.collectAsStateWithLifecycle(false)
    val refreshProgress by ReleaseRefreshRuntime.progress.collectAsStateWithLifecycle(RefreshProgress(0, 0))
    val scanInterval by prefs.scanInterval.collectAsStateWithLifecycle(ScanInterval.OnDemand)
    val lastScanAt by prefs.lastScanAtMs.collectAsStateWithLifecycle(null)
    var skipped by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var grantedNow by remember { mutableStateOf(UsageStatsAccess.isGranted(context)) }
    LifecycleResumeEffect(Unit) {
        grantedNow = UsageStatsAccess.isGranted(context)
        if (grantedNow) scope.launch { prefs.setUsageStatsConsent(UsageStatsConsent.Granted) }
        onPauseOrDispose { }
    }
    val canScan = QueryAllPackagesGate.canScan(acknowledged, Build.VERSION.SDK_INT)
    LaunchedEffect(scanInterval, lastScanAt, canScan) {
        ScanSchedule.apply(context, scanInterval)
        if (canScan && !refreshing && ScanSchedule.due(scanInterval, lastScanAt, System.currentTimeMillis())) {
            requestRefreshNotifications(context)
            ReleaseRefreshService.start(context)
        }
    }
    val nowMs = System.currentTimeMillis()
    val usage = remember(grantedNow, revision) {
        if (!grantedNow) emptyMap()
        else UsageStatsManagerCatalog(context.getSystemService(UsageStatsManager::class.java))
            .usageSince(nowMs - UsagePulse.WINDOW_MS, nowMs)
            .associateBy { it.packageName }
    }
    val installed = remember(canScan, revision) {
        if (!canScan) emptyList()
        else catalog.listInstalled().map(RemoteReleaseMemory::merge)
    }
    val visible = InventoryPresent.visible(
        apps = installed,
        includeSystem = includeSystem,
        query = query,
        staleOnly = staleOnly,
        updatesOnly = updatesOnly,
        sourceFilters = sourceFilters,
        sortMode = if (sortMode == InventorySortMode.UsedAndStale && !grantedNow) {
            InventorySortMode.Oldest
        } else {
            sortMode
        },
        usageByPackage = usage,
        nowMs = nowMs,
    )
    return InventoryUiModel(
        apps = if (canScan) visible else emptyList(),
        canScan = canScan,
        rationaleSkipped = skipped && !canScan,
        showUsageWalkthrough = canScan && consent == UsageStatsConsent.NotOffered,
        query = query,
        sortMode = sortMode,
        staleOnly = staleOnly,
        updatesOnly = updatesOnly,
        sourceFilters = sourceFilters,
        selectedApp = selectedPackage?.let { pkg -> installed.find { it.packageName == pkg } },
        canRankByUsage = grantedNow,
        showSearch = showSearch,
        showFilters = showFilters,
        refreshing = refreshing,
        refreshDone = refreshProgress.done,
        refreshTotal = refreshProgress.total,
        refreshLocation = refreshProgress.location,
        onAcknowledge = { scope.launch { prefs.setQueryAllPackagesAcknowledged(true) } },
        onSkip = { skipped = true },
        onDismissUsage = { scope.launch { prefs.setUsageStatsConsent(UsageStatsConsent.WalkthroughSeen) } },
        onQueryChange = { query = it },
        onSortMode = { mode -> scope.launch { prefs.setSortMode(mode) } },
        onStaleOnlyChange = { value -> scope.launch { prefs.setStaleOnly(value) } },
        onUpdatesOnlyChange = { value -> scope.launch { prefs.setUpdatesOnly(value) } },
        onToggleSourceFilter = { source ->
            scope.launch { prefs.setSourceFilters(InventorySourceFilter.toggle(sourceFilters, source)) }
        },
        onExport = { format -> InventoryShare.send(context, visible, format) },
        onSelect = { pkg -> selectedPackage = pkg },
        onClearSelect = { selectedPackage = null },
        onOpenUsageAccess = { UsageStatsAccess.openSettings(context) },
        onToggleSearch = {
            showSearch = !showSearch
            if (!showSearch) query = ""
        },
        onToggleFilters = { showFilters = !showFilters },
        onRefresh = {
            if (!refreshing && canScan) {
                requestRefreshNotifications(context)
                ReleaseRefreshService.start(context)
            }
        },
    )
}

private fun requestRefreshNotifications(context: Context) {
    if (Build.VERSION.SDK_INT < 33) return
    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    if (granted) return
    val activity = context as? Activity ?: return
    activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 71)
}
