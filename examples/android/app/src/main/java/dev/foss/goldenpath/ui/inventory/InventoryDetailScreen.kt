package dev.foss.goldenpath.ui.inventory

import android.os.Build
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.AppDetailsIntent
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.StoreListingIntent
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.StoreSelection
import dev.foss.goldenpath.inventory.UpdateInventory
import dev.foss.goldenpath.inventory.UpdateLink
import dev.foss.goldenpath.staleness.Staleness
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.text.DateFormat
import java.util.Date

@Composable
fun InventoryDetailScreen(
    app: InstalledApp,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val version = app.versionName ?: stringResource(R.string.inventory_version_unknown)
    val sdkRisk = Staleness.compatibilityWarning(app.targetSdk, Build.VERSION.SDK_INT)
    val listings = StoreSelection.visible(app.latestListings, rememberEnabledSources())
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.inventory_detail_back))
        }
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
            IconButton(onClick = { AppDetailsIntent.open(context, app.packageName) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.inventory_open_system_details),
                )
            }
        }
        DetailCallout(stringResource(R.string.inventory_callout_package), app.packageName)
        DetailCallout(stringResource(R.string.inventory_callout_origin), stringResource(InventoryCopy.originRes(app)))
        DetailCallout(stringResource(R.string.inventory_callout_installed), version)
        DetailCallout(stringResource(R.string.inventory_callout_installed_date), localDate(app.installedAtMs))
        DetailCallout(stringResource(R.string.inventory_callout_latest), latestText(app))
        DetailCallout(stringResource(R.string.inventory_callout_last_release), lastReleaseText(app))
        DetailCallout(
            label = stringResource(R.string.inventory_callout_sdk),
            value = stringResource(R.string.inventory_sdk, app.minSdk, app.targetSdk),
            warn = sdkRisk,
        )
        if (sdkRisk) {
            Text(text = stringResource(R.string.inventory_sdk_risk), color = MaterialTheme.colorScheme.error)
        }
        Text(text = stringResource(R.string.inventory_listings_title), style = MaterialTheme.typography.titleMedium)
        if (listings.isEmpty()) {
            Text(text = stringResource(R.string.inventory_no_listings))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                listings.forEach { StoreListingRow(it, app.packageName) }
            }
        }
        UpdateNotesSection(app.packageName)
        DownloadUpdateSection(app)
        TextButton(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.inventory_detail_back))
        }
    }
}

@Composable
private fun DetailCallout(label: String, value: String, warn: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StoreListingRow(link: UpdateLink, packageName: String) {
    val context = LocalContext.current
    val mark = InventoryCopy.listingMark(link.listed, link.known)
    val sourceName = stringResource(InventoryCopy.sourceRes(link.source))
    val source = InventoryCopy.listingMarkPrefix(mark) + sourceName
    val rowCd = stringResource(
        R.string.inventory_listing_row_cd,
        sourceName,
        stringResource(InventoryCopy.listingMarkStatusRes(mark)),
    )
    val line = if (link.listed) {
        stringResource(
            R.string.inventory_listing_line,
            link.versionName ?: stringResource(R.string.inventory_version_unknown),
            remoteDate(link.releasedAtMs),
        )
    } else {
        stringResource(InventoryCopy.unlistedRes(link.known))
    }
    val canOpen = UpdateInventory.canOpen(link)
    val tone = when {
        link.listed -> MaterialTheme.colorScheme.onSurface
        link.known -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = rowCd },
    ) {
        Text(text = source, style = MaterialTheme.typography.titleSmall)
        Text(
            text = line,
            style = MaterialTheme.typography.bodySmall,
            color = tone,
            modifier = if (canOpen) {
                Modifier.clickable(role = Role.Button) {
                    link.url?.let { StoreListingIntent.open(context, it, link.source, packageName) }
                }
            } else {
                Modifier
            },
        )
    }
}

@Composable
private fun latestText(app: InstalledApp): String {
    val remote = app.remoteVersionName
    if (remote.isNullOrBlank()) return stringResource(R.string.inventory_latest_unknown)
    return stringResource(R.string.inventory_latest_ver, remote, stringResource(InventoryCopy.sourceRes(app.remoteVersionSource)))
}

@Composable
private fun lastReleaseText(app: InstalledApp): String {
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
private fun localDate(ms: Long?): String {
    if (ms == null) return stringResource(R.string.inventory_updated_unknown)
    return DateFormat.getDateInstance().format(Date(ms))
}

@Composable
private fun remoteDate(ms: Long?): String {
    if (ms == null || ms <= 0L) return stringResource(R.string.inventory_source_date_unknown)
    return DateFormat.getDateInstance().format(Date(ms))
}
