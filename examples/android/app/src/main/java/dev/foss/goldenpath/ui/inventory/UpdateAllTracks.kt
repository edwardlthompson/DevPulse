package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.UpdateAllSegment
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.inventory.UpdateAllTally
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import dev.foss.goldenpath.ui.theme.SpacingXs

@Composable
fun UpdateAllTracks(snaps: List<UpdateAllSnap>, modifier: Modifier = Modifier) {
    val counts = UpdateAllTally.of(snaps)
    val download = counts.downloadBar()
    val install = counts.installBar()
    val downloaded = stringResource(
        R.string.update_all_downloaded,
        download.ok,
        download.total,
        download.fail,
    )
    val installed = stringResource(
        R.string.update_all_installed,
        install.ok,
        install.total,
        install.fail,
    )
    Column(modifier = modifier.fillMaxWidth()) {
        UpdateAllTrack(label = downloaded, segment = download)
        UpdateAllTrack(
            label = installed,
            segment = install,
            modifier = Modifier.padding(top = SpacingMd),
        )
    }
}

@Composable
private fun UpdateAllTrack(label: String, segment: UpdateAllSegment, modifier: Modifier = Modifier) {
    val done = (segment.ok + segment.fail).toFloat()
    val range = segment.total.coerceAtLeast(1).toFloat()
    val okColor = MaterialTheme.colorScheme.primary
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SpacingXs)
                .height(SpacingSm)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .semantics {
                    contentDescription = label
                    progressBarRangeInfo = ProgressBarRangeInfo(done / range, 0f..1f)
                },
        ) {
            if (segment.ok > 0) {
                Box(
                    modifier = Modifier
                        .weight(segment.ok.toFloat())
                        .fillMaxHeight()
                        .background(okColor),
                )
            }
            if (segment.fail > 0) {
                Box(
                    modifier = Modifier
                        .weight(segment.fail.toFloat())
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.error),
                )
            }
            if (segment.pending > 0) {
                Box(
                    modifier = Modifier
                        .weight(segment.pending.toFloat())
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
    }
}
