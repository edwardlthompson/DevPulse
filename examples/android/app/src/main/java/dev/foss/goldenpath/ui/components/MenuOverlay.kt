package dev.foss.goldenpath.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
fun MenuOverlay(
    open: Boolean,
    modifier: Modifier = Modifier,
    parent: @Composable () -> Unit,
    child: @Composable () -> Unit,
) {
    Box(modifier) {
        Box(modifier = if (open) Modifier.clearAndSetSemantics { } else Modifier) {
            parent()
        }
        if (open) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                child()
            }
        }
    }
}
