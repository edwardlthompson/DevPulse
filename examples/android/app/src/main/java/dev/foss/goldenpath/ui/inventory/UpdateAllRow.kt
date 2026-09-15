package dev.foss.goldenpath.ui.inventory

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InstallWhy
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.PlayStoreIntent
import dev.foss.goldenpath.inventory.UpdateAllBar
import dev.foss.goldenpath.inventory.UpdateAllPhase
import dev.foss.goldenpath.inventory.UpdateAllRowFill
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import dev.foss.goldenpath.ui.theme.SpacingXs

@Composable
internal fun UpdateAllRow(snap: UpdateAllSnap, rootInstall: Boolean) {
    val context = LocalContext.current
    val status = when (snap.phase) {
        UpdateAllPhase.Wait -> ""
        UpdateAllPhase.Fetch -> stringResource(R.string.update_cache_busy)
        UpdateAllPhase.Ready -> stringResource(R.string.update_all_ready)
        UpdateAllPhase.Apply -> stringResource(
            if (rootInstall) R.string.update_all_root_wait else R.string.update_cache_install,
        )
        UpdateAllPhase.Ok -> stringResource(R.string.store_client_status_installed)
        UpdateAllPhase.Fail -> stringResource(InventoryCopy.failRes(snap.failWhy, snap.source))
    }
    val playOpen = snap.phase == UpdateAllPhase.Fail &&
        (snap.failWhy == InstallWhy.PlayPurchase || snap.failWhy == InstallWhy.PlayStore)
    val bytes = fetchBytes(snap)
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = SpacingMd)) {
        Text(text = snap.label, style = MaterialTheme.typography.titleSmall)
        Text(
            text = stringResource(InventoryCopy.sourceRes(snap.source)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (status.isNotEmpty()) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = if (snap.phase == UpdateAllPhase.Fail) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        if (playOpen) {
            TextButton(onClick = { PlayStoreIntent.open(context, snap.packageName) }) {
                Text(text = stringResource(R.string.update_all_play_purchase_open))
            }
        }
        if (bytes != null) {
            Text(
                text = bytes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        UpdateAllSnapBar(
            label = stringResource(R.string.update_all_download_bar),
            bar = UpdateAllRowFill.download(snap),
        )
        UpdateAllSnapBar(
            label = stringResource(R.string.update_all_install_bar),
            bar = UpdateAllRowFill.install(snap),
        )
    }
}

@Composable
private fun UpdateAllSnapBar(label: String, bar: UpdateAllBar) {
    val color = if (bar.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.fillMaxWidth().padding(top = SpacingXs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val mod = Modifier
            .fillMaxWidth()
            .padding(top = SpacingXs)
            .semantics { contentDescription = label }
        if (bar.indeterminate) {
            LinearProgressIndicator(modifier = mod, color = color)
        } else {
            LinearProgressIndicator(
                progress = { bar.progress.coerceIn(0f, 1f) },
                modifier = mod,
                color = color,
            )
        }
    }
}

@Composable
private fun fetchBytes(snap: UpdateAllSnap): String? {
    if (snap.phase != UpdateAllPhase.Fetch || snap.received <= 0L) return null
    val context = LocalContext.current
    val have = Formatter.formatFileSize(context, snap.received)
    if (snap.expected <= 0L) return have
    return stringResource(R.string.update_all_bytes_of, have, Formatter.formatFileSize(context, snap.expected))
}
