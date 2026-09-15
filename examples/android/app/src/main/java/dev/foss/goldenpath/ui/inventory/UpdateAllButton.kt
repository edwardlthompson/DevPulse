package dev.foss.goldenpath.ui.inventory

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import dev.foss.goldenpath.inventory.SignerReplaceQueue
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.MeteredNet
import dev.foss.goldenpath.inventory.WelcomeNeeds
import dev.foss.goldenpath.inventory.UpdateAllCancel
import dev.foss.goldenpath.inventory.UpdateAllPick
import dev.foss.goldenpath.inventory.UpdateAllPhase
import dev.foss.goldenpath.inventory.UpdateAllSession
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.inventory.UpdateAllTally
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UpdateAllButton(
    apps: List<InstalledApp>,
    selected: Set<String> = emptySet(),
    startKick: Int = 0,
    modifier: Modifier = Modifier,
) {
    val revision by UpdateArtifactMemory.revision.collectAsStateWithLifecycle()
    val ignoredRev by IgnoredUpdates.revision.collectAsStateWithLifecycle(0)
    val signingRev by SignerReplaceQueue.revision.collectAsStateWithLifecycle(0)
    val deviceSdk = android.os.Build.VERSION.SDK_INT
    val deviceAbis = remember { android.os.Build.SUPPORTED_ABIS.toSet() }
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val aurora by prefs.auroraPlayEnabled.collectAsStateWithLifecycle(false)
    val groups = remember(apps, selected, revision, ignoredRev, signingRev, aurora) {
        UpdateAllPick.groups(apps, selected, deviceSdk, deviceAbis, aurora)
    }
    val queue = remember(groups) { groups.map { it.first() } }
    if (queue.isEmpty()) return
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var metered by remember { mutableStateOf(false) }
    val snaps = remember { mutableStateListOf<UpdateAllSnap>() }
    val tally = UpdateAllTally.of(snaps)
    val label = if (busy) {
        stringResource(
            R.string.update_all_busy,
            tally.downloadedOk + tally.downloadedFail,
            tally.total.coerceAtLeast(queue.size),
        )
    } else {
        stringResource(R.string.update_all, queue.size)
    }
    val start = {
        if (busy) {
            UpdateAllSession.show()
        } else if (method != InstallMethod.Session && !WelcomeNeeds.ensureInstall(context)) {
            Unit
        } else {
            UpdateAllCancel.arm()
            busy = true
            UpdateAllSession.busy.value = true
            UpdateAllSession.rootInstall.value = method == InstallMethod.Root
            UpdateAllSession.show()
            snaps.clear()
            snaps.addAll(queue.map { UpdateAllSnap(it.packageName, it.label, it.source, UpdateAllPhase.Wait) })
            UpdateAllSession.snaps.value = snaps.toList()
            startUpdateAll(
                context, scope, queue, groups, method,
                onSnap = { snap ->
                    scope.launch(Dispatchers.Main.immediate) {
                        val at = snaps.indexOfFirst { it.packageName == snap.packageName }
                        if (at >= 0) snaps[at] = snap else snaps.add(snap)
                        UpdateAllSession.snaps.value = snaps.toList()
                    }
                },
                onDone = {
                    busy = false
                    UpdateAllSession.busy.value = false
                },
            )
        }
    }
    var lastKick by remember { mutableIntStateOf(0) }
    LaunchedEffect(startKick, queue.size) {
        if (startKick > lastKick && !busy && queue.isNotEmpty()) {
            lastKick = startKick
            if (MeteredNet.needsConfirm(MeteredNet.metered(context), queue.size)) {
                metered = true
            } else {
                start()
            }
        }
    }
    TextButton(
        onClick = {
            if (busy) {
                UpdateAllSession.show()
                return@TextButton
            }
            if (MeteredNet.needsConfirm(MeteredNet.metered(context), queue.size)) {
                metered = true
            } else {
                start()
            }
        },
        enabled = !busy,
        modifier = modifier.semantics { contentDescription = label },
    ) { Text(label) }
    if (metered) {
        AlertDialog(
            onDismissRequest = { metered = false },
            title = { Text(stringResource(R.string.update_all, queue.size)) },
            text = { Text(stringResource(R.string.update_all_metered)) },
            confirmButton = {
                TextButton(onClick = { metered = false; start() }) {
                    Text(stringResource(R.string.inventory_rationale_ack))
                }
            },
            dismissButton = {
                TextButton(onClick = { metered = false }) {
                    Text(stringResource(R.string.about_not_now))
                }
            },
        )
    }
}
