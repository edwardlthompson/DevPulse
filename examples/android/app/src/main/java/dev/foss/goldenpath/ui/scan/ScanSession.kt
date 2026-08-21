package dev.foss.goldenpath.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.scan.LocalScan
import dev.foss.goldenpath.scan.ScanDetail
import dev.foss.goldenpath.scan.ScanItem
import dev.foss.goldenpath.scan.ScanMachine
import dev.foss.goldenpath.scan.ScanPhase
import dev.foss.goldenpath.notify.WidgetRedCount
import dev.foss.goldenpath.query.ScanHistoryStore
import dev.foss.goldenpath.query.ScanHistoryWrite
import dev.foss.goldenpath.scan.ScanProgress
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanSession(
    val visible: Boolean,
    val items: List<ScanItem>,
    val progress: ScanProgress,
    val selected: ScanDetail?,
    val quiet: List<String>,
    val open: () -> Unit,
    val close: () -> Unit,
    val start: () -> Unit,
    val pause: () -> Unit,
    val resume: () -> Unit,
    val select: (ScanItem) -> Unit,
    val clearSelect: () -> Unit,
)

@Composable
fun rememberScanSession(apps: List<InstalledApp>): ScanSession {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { InventoryPreferences(context) }
    val aptoideOn by prefs.aptoideLookupEnabled.collectAsStateWithLifecycle(false)
    var visible by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(ScanMachine.idle()) }
    var items by remember { mutableStateOf<List<ScanItem>>(emptyList()) }
    var selected by remember { mutableStateOf<ScanDetail?>(null) }
    var quiet by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(progress.phase, apps) {
        while (progress.phase == ScanPhase.Running) {
            delay(40)
            progress = ScanMachine.advance(progress)
        }
    }
    return ScanSession(
        visible = visible,
        items = items,
        progress = progress,
        selected = selected,
        quiet = quiet,
        open = { visible = true },
        close = {
            visible = false
            selected = null
        },
        start = {
            scope.launch {
                runCatching {
                    val now = System.currentTimeMillis()
                    if (aptoideOn) {
                        withContext(Dispatchers.IO) {
                            AptoideScan.picksFor(apps.map { it.packageName }, AptoideHttpFetcher, now)
                        }
                    }
                    val merged = apps.map { app -> runCatching { RemoteReleaseMemory.merge(app) }.getOrDefault(app) }
                    val scanned = LocalScan.run(merged, now)
                    val dir = File(context.filesDir, "scan-history")
                    ScanHistoryWrite.afterScan(dir, scanned)
                    WidgetRedCount.refresh(context)
                    quiet = ScanHistoryStore(dir).lastQuiet()
                    items = scanned
                    progress = ScanMachine.start(items.size)
                }
            }
        },
        pause = { progress = ScanMachine.pause(progress) },
        resume = { progress = ScanMachine.resume(progress) },
        select = { selected = ScanDetail(item = it) },
        clearSelect = { selected = null },
    )
}
