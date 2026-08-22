package dev.foss.goldenpath.ui.inventory

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InstallAwait
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.ListingInstallLive
import dev.foss.goldenpath.inventory.WelcomeNeeds
import dev.foss.goldenpath.inventory.OneClickResult
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.UpdateAll
import dev.foss.goldenpath.inventory.UpdateAllPick
import dev.foss.goldenpath.inventory.UpdateAllPhase
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UpdateAllButton(apps: List<InstalledApp>, modifier: Modifier = Modifier) {
    val revision by UpdateArtifactMemory.revision.collectAsStateWithLifecycle()
    val ignoredRev by IgnoredUpdates.revision.collectAsStateWithLifecycle(0)
    val groups = remember(apps, revision, ignoredRev) { UpdateAllPick.groups(apps) }
    val queue = remember(groups) { groups.map { it.first() } }
    if (queue.isEmpty()) return
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var show by remember { mutableStateOf(false) }
    val snaps = remember { mutableStateListOf<UpdateAllSnap>() }
    val finished = snaps.count { !it.stay }
    val label = if (busy) {
        stringResource(R.string.update_all_busy, finished, queue.size)
    } else {
        stringResource(R.string.update_all)
    }
    TextButton(
        onClick = {
            if (busy) return@TextButton
            if (!WelcomeNeeds.ensureInstall(context)) return@TextButton
            busy = true
            show = true
            snaps.clear()
            snaps.addAll(
                queue.map { job ->
                    UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Wait)
                },
            )
            scope.launch {
                withContext(Dispatchers.IO) {
                    Log.i("DevPulse", "update all start ${queue.size}")
                    RefreshTrace.emit = { Log.i("DevPulse", it) }
                    val result = UpdateAll.run(
                        jobs = queue,
                        groups = groups,
                        prepare = { job, progress ->
                            ListingInstallLive.prepare(context, job.packageName, job.source, job.pageUrl, progress)
                        },
                        install = { files ->
                            if (method == InstallMethod.Session) {
                                InstallAwait.arm()
                                val launched = ListingInstallLive.install(
                                    context,
                                    files,
                                    method,
                                ) == OneClickResult.Installed
                                launched && InstallAwait.await()
                            } else {
                                ListingInstallLive.install(context, files, method) ==
                                    OneClickResult.Installed
                            }
                        },
                        onSnap = { snap ->
                            scope.launch(Dispatchers.Main.immediate) {
                                val at = snaps.indexOfFirst { it.packageName == snap.packageName }
                                if (at >= 0) snaps[at] = snap else snaps.add(snap)
                            }
                        },
                        filesDir = context.filesDir,
                    )
                    Log.i(
                        "DevPulse",
                        "update all done downloaded=${result.downloaded} installed=${result.installed} failDl=${result.failedDownload} failIns=${result.failedInstall}",
                    )
                }
                busy = false
            }
        },
        enabled = !busy,
        modifier = modifier.semantics { contentDescription = label },
    ) { Text(label) }
    if (show) {
        UpdateAllDialog(
            snaps = snaps.toList(),
            complete = !busy,
            onDismiss = {
                show = false
                InstallAwait.signal(false)
            },
        )
    }
}
