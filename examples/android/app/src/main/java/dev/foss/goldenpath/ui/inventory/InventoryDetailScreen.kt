package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.AppDetailsIntent
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.StoreSelection
import dev.foss.goldenpath.query.PinPreferences
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@Composable
fun InventoryDetailScreen(
    app: InstalledApp,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    inventory: List<InstalledApp> = emptyList(),
) {
    val context = LocalContext.current
    val pinPrefs = remember { PinPreferences(context) }
    val scope = rememberCoroutineScope()
    val pins by pinPrefs.pins.collectAsStateWithLifecycle(emptySet())
    val pinned = app.packageName in pins
    val listings = StoreSelection.visible(app.latestListings, rememberEnabledSources())
    var advanced by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .highRefreshScroll()
            .padding(SpacingMd)
            .bottomInsetPadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(packageName = app.packageName, label = app.label)
            Text(
                text = app.label,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = SpacingMd),
            )
            IconButton(
                onClick = { scope.launch { pinPrefs.setPinned(app.packageName, !pinned) } },
            ) {
                Icon(
                    imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = stringResource(
                        if (pinned) R.string.inventory_unpin else R.string.inventory_pin,
                    ),
                )
            }
            IconButton(onClick = { AppDetailsIntent.open(context, app.packageName) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.inventory_open_system_details),
                )
            }
        }
        InventoryDetailCallout(stringResource(R.string.inventory_callout_package), app.packageName)
        InventoryDetailCallout(stringResource(R.string.inventory_callout_last_release), inventoryLastReleaseText(app))
        Text(text = stringResource(R.string.inventory_listings_title), style = MaterialTheme.typography.titleMedium)
        ReprobeButton(app)
        if (listings.isEmpty()) {
            Text(text = stringResource(R.string.inventory_no_listings))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                listings.forEach {
                    StoreListingRow(it, app.packageName, app.versionName, app.versionCode, app.label, app.isSystemApp)
                }
            }
        }
        TextButton(onClick = { advanced = !advanced }) {
            Text(stringResource(R.string.inventory_advanced))
        }
        if (advanced) {
            InventoryDetailAdvanced(app = app, inventory = inventory)
        }
    }
}

@Composable
internal fun inventoryLatestText(app: InstalledApp): String {
    val remote = app.remoteVersionName
    if (remote.isNullOrBlank()) return stringResource(R.string.inventory_latest_unknown)
    return stringResource(
        R.string.inventory_latest_ver,
        remote,
        stringResource(InventoryCopy.sourceRes(app.remoteVersionSource)),
    )
}

@Composable
internal fun inventoryLastReleaseText(app: InstalledApp): String {
    val ms = app.remoteReleasedAtMs
    if (ms == null || app.remoteReleasedSource == RemoteReleasedSource.None) {
        return stringResource(R.string.inventory_last_release_unknown)
    }
    return stringResource(
        R.string.inventory_last_release,
        stringResource(InventoryCopy.sourceRes(app.remoteReleasedSource)),
        DateFormat.getDateInstance().format(Date(ms)),
    )
}

@Composable
internal fun inventoryLocalDate(ms: Long?): String {
    if (ms == null) return stringResource(R.string.inventory_updated_unknown)
    return DateFormat.getDateInstance().format(Date(ms))
}
