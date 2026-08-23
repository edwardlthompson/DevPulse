package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ForgetPackage
import dev.foss.goldenpath.inventory.StaleSnooze
import java.io.File

@Composable
fun DetailForget(packageName: String) {
    val context = LocalContext.current
    Row {
        TextButton(
            onClick = { ForgetPackage.wipe(packageName, context.filesDir) },
        ) { Text(stringResource(R.string.forge_token_remove)) }
        TextButton(
            onClick = {
                val file = File(context.filesDir, "stale_snooze.tsv")
                val rows = StaleSnooze.load(file).toMutableMap()
                rows[packageName] = StaleSnooze.until(System.currentTimeMillis())
                StaleSnooze.save(file, rows)
            },
        ) { Text(stringResource(R.string.about_later)) }
    }
}
