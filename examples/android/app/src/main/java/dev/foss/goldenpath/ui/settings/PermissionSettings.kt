package dev.foss.goldenpath.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.WelcomeNeed
import dev.foss.goldenpath.inventory.WelcomeNeeds
import dev.foss.goldenpath.ui.inventory.PermissionRows
import dev.foss.goldenpath.ui.inventory.rememberPermissionGrants
import kotlinx.coroutines.launch

@Composable
fun PermissionSettings() {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val scope = rememberCoroutineScope()
    val acknowledged by prefs.queryAllPackagesAcknowledged.collectAsStateWithLifecycle(false)
    val grants = rememberPermissionGrants(appsStart = acknowledged)
    val rows = WelcomeNeeds.rows(
        appsAccepted = acknowledged || grants.apps,
        notifyGranted = grants.notify,
        installGranted = grants.install,
        usageGranted = grants.usage,
    )
    PermissionRows(rows = rows, retap = true, onClick = { need ->
        grants.onNeed(need)
        if (need == WelcomeNeed.Apps) {
            scope.launch { prefs.setQueryAllPackagesAcknowledged(true) }
        }
    })
}
