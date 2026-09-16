package dev.foss.goldenpath.ui.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.inventory.InstallAwait
import dev.foss.goldenpath.inventory.ScanUpdateCta
import dev.foss.goldenpath.inventory.UpdateAll
import dev.foss.goldenpath.inventory.UpdateAllCancel
import dev.foss.goldenpath.inventory.UpdateAllSession

@Composable
fun PulseRunHost(
    inventory: InventoryUiModel,
    lookupDone: Boolean,
    onKickUpdate: () -> Unit,
) {
    val visible by UpdateAllSession.visible.collectAsStateWithLifecycle()
    val snaps by UpdateAllSession.snaps.collectAsStateWithLifecycle()
    val busy by UpdateAllSession.busy.collectAsStateWithLifecycle()
    val rootInstall by UpdateAllSession.rootInstall.collectAsStateWithLifecycle()
    LaunchedEffect(inventory.refreshing, inventory.updateAllCount) {
        if (inventory.refreshing) {
            UpdateAllSession.seedWait(UpdateAll.jobs(inventory.apps))
        }
    }
    LaunchedEffect(lookupDone, inventory.refreshing, busy, snaps) {
        if (
            ScanUpdateCta.hideWhenIdle(
                lookupDone,
                inventory.refreshing,
                busy,
                snaps,
            )
        ) {
            inventory.onDismissRefresh()
            UpdateAllSession.hide()
        }
    }
    if (!inventory.canScan) return
    if (!inventory.showRefreshDialog && !visible) return
    val complete = !busy && (!inventory.refreshing || lookupDone)
    val updateCount = inventory.updateAllCount
    UpdateAllDialog(
        snaps = snaps,
        complete = complete,
        rootInstall = rootInstall,
        scan = if (inventory.showRefreshDialog) {
            PulseScanHeader(
                done = inventory.refreshDone,
                total = inventory.refreshTotal,
                location = inventory.refreshLocation,
                firstScan = inventory.firstRefresh,
                outlets = inventory.refreshOutlets,
                scanning = inventory.refreshing && !lookupDone,
            )
        } else {
            null
        },
        updateCount = updateCount,
        onUpdate = if (complete && ScanUpdateCta.visible(lookupDone, updateCount) && !busy) {
            onKickUpdate
        } else {
            null
        },
        onStopOutlet = inventory.onStopOutlet,
        onHide = {
            inventory.onDismissRefresh()
            UpdateAllSession.hide()
        },
        onStop = {
            inventory.onDismissRefresh()
            UpdateAllSession.hide()
            UpdateAllCancel.request()
            InstallAwait.signal(false)
        },
    )
}
