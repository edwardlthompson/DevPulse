package dev.foss.goldenpath.ui.inventory

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.format.Formatter
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InstallWhy
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.PlayStoreIntent
import dev.foss.goldenpath.inventory.SignerReplaceQueue
import dev.foss.goldenpath.inventory.SignerReplaceStore
import dev.foss.goldenpath.inventory.UpdateAllPhase
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.inventory.UpdateAllTally
import dev.foss.goldenpath.ui.theme.ElevationLevel2
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun UpdateAllDialog(
    snaps: List<UpdateAllSnap>,
    complete: Boolean,
    onDismiss: () -> Unit,
) {
    val rows = UpdateAllTally.ranked(snaps)
    val context = LocalContext.current
    val signingRev by SignerReplaceQueue.revision.collectAsStateWithLifecycle(0)
    val holds = remember(signingRev) { SignerReplaceQueue.rows }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BackHandler { onDismiss() }
        dimBehind()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(SpacingMd),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = ElevationLevel2,
                shadowElevation = ElevationLevel2,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(SpacingMd)) {
                    Text(
                        text = stringResource(R.string.update_all, snaps.size),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    UpdateAllTracks(snaps = snaps, modifier = Modifier.padding(top = SpacingSm))
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = SpacingMd)) {
                        items(rows, key = { it.packageName }) { snap ->
                            UpdateAllRow(snap)
                        }
                        if (complete && holds.isNotEmpty()) {
                            item(key = "signing-issues") {
                                Text(
                                    text = stringResource(R.string.signer_replace_list, holds.size),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = SpacingMd, bottom = SpacingSm),
                                )
                                SigningIssueBlock(
                                    holds = holds,
                                    onReplace = { hold ->
                                        IgnoredUpdates.drop(hold.packageName, context.filesDir)
                                        SignerReplaceStore.save(context.filesDir, hold)
                                        onDismiss()
                                    },
                                )
                            }
                        }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text(text = stringResource(R.string.about_close))
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAllRow(snap: UpdateAllSnap) {
    val context = LocalContext.current
    val status = when (snap.phase) {
        UpdateAllPhase.Wait -> ""
        UpdateAllPhase.Fetch -> stringResource(R.string.update_cache_busy)
        UpdateAllPhase.Ready -> stringResource(R.string.update_all_ready)
        UpdateAllPhase.Apply -> stringResource(R.string.update_cache_install)
        UpdateAllPhase.Ok -> stringResource(R.string.store_client_status_installed)
        UpdateAllPhase.Fail -> stringResource(failStatus(snap))
    }
    val bytes = fetchBytes(snap)
    val playOpen = snap.phase == UpdateAllPhase.Fail &&
        (snap.failWhy == InstallWhy.PlayPurchase || snap.failWhy == InstallWhy.PlayStore)
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
        if (snap.phase == UpdateAllPhase.Fetch) {
            if (snap.expected > 0L) {
                LinearProgressIndicator(
                    progress = { (snap.received.toFloat() / snap.expected.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = SpacingSm),
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = SpacingSm))
            }
        }
    }
}

private fun failStatus(snap: UpdateAllSnap): Int = when (snap.failWhy) {
    InstallWhy.Signing -> R.string.sources_no_install
    InstallWhy.Permission -> R.string.install_method_failed
    InstallWhy.Timeout, InstallWhy.Older, InstallWhy.Sdk, InstallWhy.NoFile -> R.string.update_cache_failed
    InstallWhy.NoSpace -> R.string.update_all_no_space
    InstallWhy.PlayPurchase -> R.string.update_all_play_purchase
    InstallWhy.PlayStore -> R.string.update_all_play_store
}

@Composable
private fun fetchBytes(snap: UpdateAllSnap): String? {
    if (snap.phase != UpdateAllPhase.Fetch || snap.received <= 0L) return null
    val context = LocalContext.current
    val have = Formatter.formatFileSize(context, snap.received)
    if (snap.expected <= 0L) return have
    return stringResource(R.string.update_all_bytes_of, have, Formatter.formatFileSize(context, snap.expected))
}

@Composable
private fun dimBehind() {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.setDimAmount(0.45f)
        if (Build.VERSION.SDK_INT >= 31) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = 32 }
        }
    }
}
