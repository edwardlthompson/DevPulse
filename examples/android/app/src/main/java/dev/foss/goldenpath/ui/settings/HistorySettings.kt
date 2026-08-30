package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.AirplaneCopy
import dev.foss.goldenpath.inventory.AirplaneMode
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.OutletLabels
import dev.foss.goldenpath.inventory.PulseHistory
import dev.foss.goldenpath.inventory.PulseHistoryFormat
import dev.foss.goldenpath.inventory.PulseHistoryView
import dev.foss.goldenpath.inventory.UpdateAllLog
import dev.foss.goldenpath.inventory.RefreshFailBook
import dev.foss.goldenpath.inventory.RefreshOutletEta
import dev.foss.goldenpath.inventory.RefreshPaceFile
import dev.foss.goldenpath.inventory.RefreshSuccessBook
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun HistorySettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    remember {
        RefreshSuccessBook.hydrate(RefreshPaceFile.load(File(context.filesDir, "refresh_success.tsv")))
        RefreshFailBook.hydrate(RefreshPaceFile.load(File(context.filesDir, "refresh_fail.tsv")))
        IgnoredUpdates.hydrate(context.filesDir)
    }
    val rev by RemoteReleaseMemory.revision.collectAsStateWithLifecycle(0)
    val pulses = PulseHistoryFormat.newestFirst(PulseHistory.load(PulseHistory.file(context.filesDir)))
    val ok = remember(rev) { RefreshSuccessBook.snapshot().entries.sortedBy { it.key } }
    val fail = remember(rev) { RefreshFailBook.snapshot().entries.sortedBy { it.key } }
    val ignoredRev by IgnoredUpdates.revision.collectAsStateWithLifecycle(0)
    val hasUpdateLog = remember(ignoredRev) {
        UpdateAllLog.failed(UpdateAllLog.file(context.filesDir)).isNotEmpty() || IgnoredUpdates.rows.isNotEmpty()
    }
    if (pulses.isEmpty() && ok.isEmpty() && fail.isEmpty() && !hasUpdateLog) {
        Text(text = stringResource(R.string.history_empty), modifier = modifier)
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        UpdateAllHistory()
        if (ok.isNotEmpty()) {
            SettingsGroup {
                Text(text = stringResource(R.string.history_last_ok), style = MaterialTheme.typography.titleSmall)
                ok.forEach { (id, at) ->
                    Text(
                        text = AirplaneCopy.tagged(sourceTime(context, id, at), AirplaneMode.on(context)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (fail.isNotEmpty()) {
            SettingsGroup {
                Text(text = stringResource(R.string.history_last_fail), style = MaterialTheme.typography.titleSmall)
                fail.forEach { (id, at) ->
                    Text(
                        text = sourceTime(context, id, at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        pulses.forEach { row ->
            val view = PulseHistoryFormat.view(row)
            SettingsGroup { PulseEvent(view) }
        }
    }
}

@Composable
private fun PulseEvent(view: PulseHistoryView) {
    val context = LocalContext.current
    val whenText = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(view.atMs))
    val took = stringResource(R.string.refresh_outlet_took, RefreshOutletEta.label(view.wallMs))
    val head = listOf(
        kindLabel(view.kind),
        whenText,
        took,
        stringResource(R.string.history_apps, view.count),
    ).joinToString(" · ")
    Text(text = head, style = MaterialTheme.typography.titleSmall)
    view.outlets.forEach { (id, ms) ->
        Text(
            text = stringResource(
                R.string.inventory_listing_line,
                outletName(context, id),
                stringResource(R.string.refresh_outlet_took, RefreshOutletEta.label(ms)),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    view.notes.forEach { (key, value) ->
        Text(
            text = noteLine(key, value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun noteLine(key: String, value: String): String = when (key) {
    "downloaded" -> stringResource(R.string.history_downloaded, value)
    "failDl" -> stringResource(R.string.history_failed_download, value)
    "failIns" -> stringResource(R.string.history_failed_install, value)
    else -> stringResource(R.string.inventory_listing_line, key, value)
}

@Composable
private fun kindLabel(kind: String): String = when (kind) {
    "refresh" -> stringResource(R.string.history_kind_refresh)
    "scan" -> stringResource(R.string.history_kind_scan)
    "update" -> stringResource(R.string.history_kind_update)
    else -> kind
}

private fun outletName(context: android.content.Context, id: String): String {
    val named = OutletLabels.nameRes(id)?.let { context.getString(it) }
    val fallback = OutletLabels.fallback(id)
    return if (named != null && id.startsWith("fdroid:")) {
        context.getString(R.string.inventory_listing_line, named, fallback)
    } else {
        named ?: fallback
    }
}

private fun sourceTime(context: android.content.Context, id: String, at: Long): String =
    context.getString(
        R.string.inventory_listing_line,
        outletName(context, id),
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(at)),
    )
