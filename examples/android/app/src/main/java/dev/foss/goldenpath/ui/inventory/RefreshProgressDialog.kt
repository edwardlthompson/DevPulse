package dev.foss.goldenpath.ui.inventory

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RefreshOutletSnap
import dev.foss.goldenpath.ui.theme.ElevationLevel2
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun RefreshProgressDialog(
    done: Int,
    total: Int,
    location: String,
    firstScan: Boolean,
    outlets: List<RefreshOutletSnap>,
    onStopOutlet: (String) -> Unit,
    complete: Boolean = false,
    onDismiss: () -> Unit = {},
) {
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
        blurBehindDialog()
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
                    RefreshProgressBar(
                        done = done,
                        total = total,
                        location = location,
                        firstScan = firstScan && !complete,
                        outlets = outlets,
                        onStopOutlet = onStopOutlet,
                        modifier = Modifier.weight(1f),
                    )
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
private fun blurBehindDialog() {
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
