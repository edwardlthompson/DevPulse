package dev.foss.goldenpath.ui.inventory

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.AirplaneCopy
import dev.foss.goldenpath.inventory.AirplaneMode
import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshPaceFile
import dev.foss.goldenpath.inventory.RefreshFailBook
import dev.foss.goldenpath.inventory.RefreshSuccessBook
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun RefreshSuccessLine() {
    val context = LocalContext.current
    remember {
        RefreshSuccessBook.hydrate(RefreshPaceFile.load(File(context.filesDir, "refresh_success.tsv")))
        RefreshFailBook.hydrate(RefreshPaceFile.load(File(context.filesDir, "refresh_fail.tsv")))
    }
    val rev by RemoteReleaseMemory.revision.collectAsStateWithLifecycle(0)
    val rows = remember(rev) {
        RefreshSuccessBook.snapshot().entries.sortedBy { it.key }
    }
    val fails = remember(rev) {
        RefreshFailBook.snapshot().entries.sortedBy { it.key }
    }
    if (rows.isEmpty() && fails.isEmpty()) return
    if (rows.isNotEmpty()) {
        Text(
            text = AirplaneCopy.tagged(outletLine(context, rows), AirplaneMode.on(context)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (fails.isNotEmpty()) {
        Text(
            text = outletLine(context, fails),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private fun outletName(context: android.content.Context, id: String): String = when (id) {
    RefreshOutletIds.PLAY -> context.getString(R.string.inventory_source_play)
    RefreshOutletIds.APTOIDE -> context.getString(R.string.inventory_source_aptoide)
    RefreshOutletIds.GITHUB -> context.getString(R.string.inventory_source_forge)
    RefreshOutletIds.MIRROR -> context.getString(R.string.inventory_source_apkmirror)
    RefreshOutletIds.PURE -> context.getString(R.string.inventory_source_apkpure)
    else -> id.removePrefix("fdroid:")
}

private fun outletLine(context: android.content.Context, rows: List<Map.Entry<String, Long>>): String =
    rows.joinToString(" · ") { (id, at) ->
        context.getString(
            R.string.inventory_listing_line,
            outletName(context, id),
            DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(at)),
        )
    }
