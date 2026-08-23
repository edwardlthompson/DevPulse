package dev.foss.goldenpath.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RefreshHostBackoff
import dev.foss.goldenpath.inventory.RefreshOutletEta
import kotlinx.coroutines.delay

@Composable
fun HostBackoffLine() {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val rows = RefreshHostBackoff.active(nowMs).entries.sortedBy { it.key }
    if (rows.isEmpty()) return
    val line = rows.joinToString(" · ") { (host, remain) ->
        "$host ${RefreshOutletEta.label(remain)}"
    }
    Text(
        text = stringResource(R.string.refresh_outlet_eta, line),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
