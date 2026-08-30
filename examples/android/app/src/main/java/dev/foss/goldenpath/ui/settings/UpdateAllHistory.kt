package dev.foss.goldenpath.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InstallWhy
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateAllLog
import dev.foss.goldenpath.inventory.UpdateAllLogRow
import dev.foss.goldenpath.inventory.UpdateAllRetry

@Composable
fun UpdateAllHistory() {
    val context = LocalContext.current
    var epoch by remember { mutableIntStateOf(0) }
    val ignoredRev by IgnoredUpdates.revision.collectAsStateWithLifecycle(0)
    val fails = remember(epoch, ignoredRev) {
        UpdateAllLog.failed(UpdateAllLog.file(context.filesDir)).asReversed()
    }
    val ignored = IgnoredUpdates.rows.size
    if (fails.isEmpty() && ignored == 0) return
    SettingsGroup {
        Text(text = stringResource(R.string.history_update_log), style = MaterialTheme.typography.titleSmall)
        if (ignored > 0) {
            Text(
                text = stringResource(R.string.history_ignored_count, ignored),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IgnoredUpdates.rows.toList().take(20).forEach { item ->
                val res = InventoryCopy.sourceRes(item.source)
                val src = stringResource(res)
                Text(
                    text = "${item.packageName} · $src · ${item.versionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = { UpdateAllRetry.ignored(context.filesDir); epoch += 1 }) {
                Text(text = stringResource(R.string.history_retry_ignored))
            }
        }
        if (fails.isNotEmpty()) {
            if (fails.any { it.result == "failDl" }) {
                TextButton(onClick = { UpdateAllRetry.downloads(context.filesDir); epoch += 1 }) {
                    Text(text = stringResource(R.string.history_retry_downloads))
                }
            }
            if (fails.any { it.result == "failIns" }) {
                TextButton(onClick = { UpdateAllRetry.installs(context.filesDir); epoch += 1 }) {
                    Text(text = stringResource(R.string.history_retry_installs))
                }
            }
            fails.take(10).forEach { row ->
                Text(
                    text = failLine(row),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun failLine(row: UpdateAllLogRow): String {
    val source = runCatching { RemoteReleasedSource.valueOf(row.source) }.getOrNull()
        ?.let { stringResource(InventoryCopy.sourceRes(it)) }
        ?: row.source
    val why = row.why.takeIf { it.isNotEmpty() && it != "NoFile" }.orEmpty()
    val mapped = runCatching { InstallWhy.valueOf(row.why) }.getOrNull()
    val result = when {
        mapped == InstallWhy.PlayPurchase -> stringResource(R.string.update_all_play_purchase)
        mapped == InstallWhy.PlayStore -> stringResource(R.string.update_all_play_store)
        row.result == "failIns" -> stringResource(R.string.install_method_failed)
        else -> stringResource(R.string.update_cache_failed)
    }
    val detail = listOf(source, result, why.takeIf { mapped != InstallWhy.PlayPurchase && mapped != InstallWhy.PlayStore }.orEmpty())
        .filter { it.isNotEmpty() }
        .joinToString(" · ")
    return stringResource(R.string.inventory_listing_line, row.label, detail)
}
