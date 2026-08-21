package dev.foss.goldenpath.ui.scan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.scan.ScanItem
import dev.foss.goldenpath.scan.ScanPhase
import dev.foss.goldenpath.scan.ScanProgress
import dev.foss.goldenpath.staleness.Badge
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun ScanScreen(
    items: List<ScanItem>,
    progress: ScanProgress,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSelect: (ScanItem) -> Unit,
    onClose: () -> Unit,
    quiet: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val visibleCount = when (progress.phase) {
        ScanPhase.Idle -> 0
        ScanPhase.Completed -> items.size
        else -> progress.completed.coerceAtMost(items.size)
    }
    val visible = items.take(visibleCount)
    val progressCd = stringResource(R.string.scan_progress, progress.completed, progress.total)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.scan_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = progressCd,
            modifier = Modifier.semantics { contentDescription = progressCd },
        )
        if (quiet.isNotEmpty()) {
            Text(text = stringResource(R.string.scan_went_quiet, quiet.size))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
            when (progress.phase) {
                ScanPhase.Running -> Button(onClick = onPause) { Text(stringResource(R.string.scan_pause)) }
                ScanPhase.Paused -> Button(onClick = onResume) { Text(stringResource(R.string.scan_resume)) }
                else -> Button(onClick = onStart) { Text(stringResource(R.string.scan_start)) }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f).highRefreshScroll()) {
            items(visible, key = { it.app.packageName }) { item ->
                ScanResultRow(item = item, onClick = { onSelect(item) })
            }
        }
        TextButton(onClick = onClose, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.scan_close))
        }
    }
}

@Composable
private fun ScanResultRow(item: ScanItem, onClick: () -> Unit) {
    val badgeCd = badgeContentDescription(item.staleness.badge)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = SpacingSm)
            .semantics { contentDescription = badgeCd },
    ) {
        Text(text = item.app.label, style = MaterialTheme.typography.titleMedium)
        Text(text = item.app.packageName, style = MaterialTheme.typography.bodySmall)
        Text(text = badgeCd, style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(R.string.scan_repo_none), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun badgeContentDescription(badge: Badge): String = stringResource(
    when (badge) {
        Badge.Green -> R.string.badge_green
        Badge.Amber -> R.string.badge_amber
        Badge.Red -> R.string.badge_red
        Badge.Unknown -> R.string.badge_unknown
    },
)
