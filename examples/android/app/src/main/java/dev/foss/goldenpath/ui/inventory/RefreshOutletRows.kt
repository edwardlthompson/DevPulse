package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RefreshOutletEta
import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshOutletSnap
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun RefreshOutletRows(
    outlets: List<RefreshOutletSnap>,
    onStop: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(items = RefreshOutletEta.sorted(outlets), key = { it.id }) { row ->
            OutletRow(row = row, onStop = onStop)
        }
    }
}

@Composable
private fun OutletRow(row: RefreshOutletSnap, onStop: (String) -> Unit) {
    val fraction = if (row.total <= 0) 0f else (row.done.toFloat() / row.total).coerceIn(0f, 1f)
    val running = !row.skipped && row.done < row.total
    Column(modifier = Modifier.fillMaxWidth().padding(top = SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title(row),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { onStop(row.id) },
                enabled = running,
                modifier = Modifier.alpha(if (running) 1f else 0f),
            ) {
                Text(text = stringResource(R.string.refresh_outlet_stop))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(
            text = detail(row),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SpacingSm),
        )
    }
}

@Composable
private fun title(row: RefreshOutletSnap): String {
    val name = when (row.id) {
        RefreshOutletIds.PLAY -> stringResource(R.string.inventory_source_play)
        RefreshOutletIds.APTOIDE -> stringResource(R.string.inventory_source_aptoide)
        RefreshOutletIds.GITHUB -> stringResource(R.string.inventory_source_forge)
        RefreshOutletIds.MIRROR -> stringResource(R.string.inventory_source_apkmirror)
        RefreshOutletIds.PURE -> stringResource(R.string.inventory_source_apkpure)
        else -> row.title
    }
    return if (row.id.startsWith("fdroid:")) {
        stringResource(R.string.inventory_listing_line, stringResource(R.string.inventory_source_fdroid), row.title)
    } else {
        name
    }
}

@Composable
private fun detail(row: RefreshOutletSnap): String {
    val counts = stringResource(R.string.scan_progress, row.done, row.total)
    val time = when {
        RefreshOutletEta.finished(row) && row.elapsedMs > 0 ->
            stringResource(R.string.refresh_outlet_took, RefreshOutletEta.label(row.elapsedMs))
        !row.skipped && (row.etaMs ?: 0) > 0 ->
            stringResource(R.string.refresh_outlet_eta, RefreshOutletEta.label(row.etaMs))
        else -> null
    }
    return listOfNotNull(counts, time).joinToString(" · ")
}
