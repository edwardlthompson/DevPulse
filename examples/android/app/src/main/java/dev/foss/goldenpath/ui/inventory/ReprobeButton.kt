package dev.foss.goldenpath.ui.inventory

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.AppReprobeLive
import dev.foss.goldenpath.inventory.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReprobeButton(app: InstalledApp) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember(app.packageName) { mutableStateOf(false) }
    val label = stringResource(if (busy) R.string.update_one_click_busy else R.string.inventory_refresh)
    TextButton(
        onClick = {
            if (busy) return@TextButton
            busy = true
            scope.launch {
                withContext(Dispatchers.IO) { AppReprobeLive.run(context, app) }
                busy = false
            }
        },
        enabled = !busy,
    ) { Text(label) }
}
