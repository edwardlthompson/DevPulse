package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.ListingFetch
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateInventory
import dev.foss.goldenpath.inventory.UpdateLink
import dev.foss.goldenpath.inventory.WelcomeNeeds
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun StoreListingRow(link: UpdateLink, packageName: String, installedVersion: String? = null) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val scope = rememberCoroutineScope()
    var busy by remember(packageName, link.source) { mutableStateOf(false) }
    var failRes by remember(packageName, link.source) { mutableStateOf<Int?>(null) }
    var received by remember(packageName, link.source) { mutableLongStateOf(0L) }
    var expected by remember(packageName, link.source) { mutableLongStateOf(-1L) }
    var pickAptoide by remember(packageName, link.source) { mutableStateOf(false) }
    val ignoredRev by IgnoredUpdates.revision.collectAsStateWithLifecycle(0)
    val ignored = remember(ignoredRev, packageName, link.source, link.versionName) {
        IgnoredUpdates.has(packageName, link.source, link.versionName)
    }
    val mark = InventoryCopy.listingMark(link.listed, link.known, ignored)
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
            listingDate(link.releasedAtMs),
        )
    } else {
        stringResource(InventoryCopy.unlistedRes(link.known, link.miss))
    }
    val deviceSdk = android.os.Build.VERSION.SDK_INT
    val deviceAbis = android.os.Build.SUPPORTED_ABIS.toSet()
    val canOpen = UpdateInventory.canOpen(link, packageName, installedVersion, deviceSdk, deviceAbis)
    val tone = when {
        ignored -> MaterialTheme.colorScheme.tertiary
        link.listed -> MaterialTheme.colorScheme.onSurface
        link.known -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    fun startFetch() {
        busy = true
        failRes = null
        received = 0L
        expected = -1L
        scope.launch {
            failRes = runCatching {
                withContext(Dispatchers.IO) {
                    ListingFetch.run(context, packageName, link, installedVersion, method) { read, total ->
                        scope.launch(Dispatchers.Main.immediate) {
                            received = read
                            expected = total
                        }
                    }
                }
            }.getOrElse {
                IgnoredUpdates.add(packageName, link.source, link.versionName, context.filesDir)
                R.string.update_cache_failed
            }
            busy = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canOpen) {
                    Modifier.clickable(enabled = !busy, role = Role.Button) {
                        if (method != InstallMethod.Session && !WelcomeNeeds.ensureInstall(context)) return@clickable
                        if (link.source == RemoteReleasedSource.Aptoide) pickAptoide = true else startFetch()
                    }
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) { contentDescription = rowCd },
    ) {
        Text(
            text = source,
            style = MaterialTheme.typography.titleSmall,
            color = if (ignored) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        )
        Text(text = line, style = MaterialTheme.typography.bodySmall, color = tone)
        ListingExtrasLine(link, installedVersion, deviceSdk, deviceAbis)
        if (pickAptoide) {
            AptoideCatalogDialog(onPicked = { pickAptoide = false; startFetch() }, onDismiss = { pickAptoide = false })
        }
        if (busy) {
            Text(text = stringResource(R.string.update_cache_busy), style = MaterialTheme.typography.bodySmall)
            if (expected > 0L) {
                LinearProgressIndicator(
                    progress = { (received.toFloat() / expected.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        failRes?.let { res ->
            Text(text = stringResource(res), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun listingDate(ms: Long?): String =
    if (ms == null || ms <= 0L) stringResource(R.string.inventory_source_date_unknown)
    else DateFormat.getDateInstance().format(Date(ms))
