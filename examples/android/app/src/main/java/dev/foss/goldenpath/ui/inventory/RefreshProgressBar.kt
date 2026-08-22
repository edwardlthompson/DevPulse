package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RefreshOutletSnap
import dev.foss.goldenpath.notify.RefreshNotifyCopy
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun RefreshProgressBar(
    done: Int,
    total: Int,
    location: String = "",
    firstScan: Boolean = false,
    outlets: List<RefreshOutletSnap> = emptyList(),
    onStopOutlet: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val fraction = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(bottom = SpacingSm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Text(
                text = if (total <= 0) {
                    stringResource(R.string.inventory_refreshing)
                } else {
                    stringResource(R.string.inventory_refresh_progress, done, total)
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = SpacingSm, top = SpacingSm, end = SpacingSm),
            )
            RefreshNotifyCopy.firstScanHintRes(firstScan)?.let { hint ->
                Text(
                    text = stringResource(hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = SpacingSm, top = SpacingSm, end = SpacingSm),
                )
            }
        }
        if (outlets.isNotEmpty()) {
            RefreshOutletRows(
                outlets = outlets,
                onStop = onStopOutlet,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = SpacingSm, end = SpacingSm, bottom = SpacingSm),
            )
        }
    }
}
