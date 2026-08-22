package dev.foss.goldenpath.ui.inventory

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.UpdateAllPhase
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.ui.theme.ElevationLevel2
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun UpdateAllDialog(
    snaps: List<UpdateAllSnap>,
    complete: Boolean,
    onDismiss: () -> Unit,
) {
    val finished = snaps.count { !it.stay }
    val total = snaps.size.coerceAtLeast(1)
    val visible = snaps.filter { it.stay }
    val current = snaps.firstOrNull { it.phase == UpdateAllPhase.Fetch && it.expected > 0L }
    val extra = if (current == null) 0f else (current.received.toFloat() / current.expected.toFloat()).coerceIn(0f, 1f)
    val fraction = ((finished + extra) / total).coerceIn(0f, 1f)
    Dialog(
        onDismissRequest = { if (complete) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = complete,
            dismissOnClickOutside = complete,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BackHandler(enabled = complete) { onDismiss() }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.TopCenter),
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(SpacingMd)) {
                    Text(text = stringResource(R.string.update_all), style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SpacingSm)
                            .height(6.dp)
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
                        text = stringResource(R.string.update_all_busy, finished, snaps.size),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = SpacingSm, bottom = SpacingMd),
                    )
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(visible, key = { it.packageName }) { snap ->
                            UpdateAllRow(snap)
                        }
                    }
                    if (complete) {
                        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                            Text(text = stringResource(R.string.about_close))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAllRow(snap: UpdateAllSnap) {
    val status = when (snap.phase) {
        UpdateAllPhase.Wait -> ""
        UpdateAllPhase.Fetch -> stringResource(R.string.update_cache_busy)
        UpdateAllPhase.Apply -> stringResource(R.string.update_cache_install)
        UpdateAllPhase.Ok -> stringResource(R.string.store_client_status_installed)
        UpdateAllPhase.Fail -> stringResource(
            if (snap.failDownload) R.string.update_cache_failed else R.string.install_method_failed,
        )
    }
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
