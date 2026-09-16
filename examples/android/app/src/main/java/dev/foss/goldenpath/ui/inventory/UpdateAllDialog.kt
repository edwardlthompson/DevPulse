package dev.foss.goldenpath.ui.inventory

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.RefreshOutletSnap
import dev.foss.goldenpath.inventory.ReleaseRefreshRuntime
import dev.foss.goldenpath.inventory.SignerReplaceQueue
import dev.foss.goldenpath.inventory.SignerReplaceStore
import dev.foss.goldenpath.inventory.UpdateAllFollow
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.inventory.UpdateAllTally
import dev.foss.goldenpath.ui.theme.ElevationLevel2
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm

data class PulseScanHeader(
    val done: Int,
    val total: Int,
    val location: String,
    val firstScan: Boolean,
    val outlets: List<RefreshOutletSnap>,
    val scanning: Boolean,
)

@Composable
fun UpdateAllDialog(
    snaps: List<UpdateAllSnap>,
    complete: Boolean,
    rootInstall: Boolean = false,
    scan: PulseScanHeader? = null,
    updateCount: Int = 0,
    onUpdate: (() -> Unit)? = null,
    onStopOutlet: (String) -> Unit = {},
    onHide: () -> Unit,
    onStop: () -> Unit,
) {
    val rows = UpdateAllTally.ranked(snaps)
    val context = LocalContext.current
    val signingRev by SignerReplaceQueue.revision.collectAsStateWithLifecycle(0)
    val holds = remember(signingRev) { SignerReplaceQueue.rows }
    val userScrolled = remember { mutableStateOf(false) }
    val follow = UpdateAllFollow.magnet(userScrolled.value)
    val listState = rememberLazyListState()
    val magnet = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) userScrolled.value = true
                return Offset.Zero
            }
        }
    }
    val paused by ReleaseRefreshRuntime.paused.collectAsStateWithLifecycle(false)
    LaunchedEffect(follow, rows.firstOrNull()?.packageName, rows.firstOrNull()?.phase) {
        if (follow && rows.isNotEmpty()) listState.scrollToItem(0)
    }
    Dialog(
        onDismissRequest = onHide,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BackHandler { onHide() }
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
                        text = when {
                            scan != null -> stringResource(R.string.pulse_run_title)
                            else -> stringResource(R.string.update_all, rows.size)
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (scan != null) {
                        RefreshProgressBar(
                            done = scan.done,
                            total = scan.total,
                            location = scan.location,
                            firstScan = false,
                            outlets = emptyList(),
                            compact = true,
                            modifier = Modifier.padding(top = SpacingSm),
                        )
                    }
                    if (rows.isNotEmpty()) {
                        UpdateAllTracks(snaps = snaps, modifier = Modifier.padding(top = SpacingSm))
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = SpacingMd)
                            .nestedScroll(magnet),
                    ) {
                        items(rows, key = { it.packageName }) { snap ->
                            UpdateAllRow(snap, rootInstall)
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
                                        onHide()
                                    },
                                )
                            }
                        }
                    }
                    Row(modifier = Modifier.align(Alignment.End)) {
                        if (scan?.scanning == true) {
                            TextButton(
                                onClick = {
                                    if (paused) ReleaseRefreshRuntime.resume() else ReleaseRefreshRuntime.pause()
                                },
                            ) {
                                Text(text = stringResource(if (paused) R.string.scan_resume else R.string.scan_pause))
                            }
                        }
                        if (complete && updateCount > 0 && onUpdate != null) {
                            TextButton(onClick = onUpdate) {
                                Text(text = stringResource(R.string.update_all, updateCount))
                            }
                        }
                        TextButton(onClick = onHide) {
                            Text(text = stringResource(R.string.update_all_hide))
                        }
                        if (!complete) {
                            TextButton(onClick = onStop) {
                                Text(text = stringResource(R.string.update_all_stop))
                            }
                        }
                    }
                }
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
