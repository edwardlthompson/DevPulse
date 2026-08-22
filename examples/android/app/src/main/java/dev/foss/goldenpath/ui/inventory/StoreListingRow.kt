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
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.OneClickResult
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.ListingInstallLive
import dev.foss.goldenpath.inventory.UpdateInventory
import dev.foss.goldenpath.inventory.UpdateLink
import dev.foss.goldenpath.inventory.WelcomeNeeds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@Composable
internal fun StoreListingRow(link: UpdateLink, packageName: String) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val scope = rememberCoroutineScope()
    var busy by remember(packageName, link.source) { mutableStateOf(false) }
    var failRes by remember(packageName, link.source) { mutableStateOf<Int?>(null) }
    var received by remember(packageName, link.source) { mutableLongStateOf(0L) }
    var expected by remember(packageName, link.source) { mutableLongStateOf(-1L) }
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
        stringResource(InventoryCopy.unlistedRes(link.known))
    }
    val canOpen = UpdateInventory.canOpen(link, packageName)
    val tone = when {
        ignored -> MaterialTheme.colorScheme.tertiary
        link.listed -> MaterialTheme.colorScheme.onSurface
        link.known -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canOpen) {
                    Modifier.clickable(enabled = !busy, role = Role.Button) {
                        if (!WelcomeNeeds.ensureInstall(context)) return@clickable
                        busy = true
                        failRes = null
                        received = 0L
                        expected = -1L
                        scope.launch {
                            try {
                                val files = withContext(Dispatchers.IO) {
                                    ListingInstallLive.prepare(
                                        context,
                                        packageName,
                                        link.source,
                                        link.url,
                                    ) { read, total ->
                                        scope.launch(Dispatchers.Main.immediate) {
                                            received = read
                                            expected = total
                                        }
                                    }
                                }
                                failRes = when (ListingInstallLive.install(context, files, method)) {
                                    OneClickResult.FailedDownload -> R.string.update_cache_failed
                                    OneClickResult.FailedInstall -> R.string.install_method_failed
                                    else -> null
                                }
                                if (failRes != null) {
                                    IgnoredUpdates.add(packageName, link.source, link.versionName, context.filesDir)
                                }
                            } catch (_: Exception) {
                                failRes = R.string.update_cache_failed
                                IgnoredUpdates.add(packageName, link.source, link.versionName, context.filesDir)
                            } finally {
                                busy = false
                            }
                        }
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
private fun listingDate(ms: Long?): String {
    if (ms == null || ms <= 0L) return stringResource(R.string.inventory_source_date_unknown)
    return DateFormat.getDateInstance().format(Date(ms))
}
