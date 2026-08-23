package dev.foss.goldenpath.ui.inventory

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.PackageShare

@Composable
fun DetailCopy(packageName: String, signingSha1: String?) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            val line = PackageShare.line(packageName, signingSha1)
            if (line.isEmpty()) return@TextButton
            context.getSystemService(ClipboardManager::class.java)
                ?.setPrimaryClip(ClipData.newPlainText(packageName, line))
        },
    ) { Text(stringResource(R.string.inventory_callout_package)) }
}
