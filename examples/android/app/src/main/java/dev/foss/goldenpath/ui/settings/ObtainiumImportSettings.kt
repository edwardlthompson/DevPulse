package dev.foss.goldenpath.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ObtainiumImportLive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ObtainiumImportSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickLabel = stringResource(R.string.forge_import_file)
    val failed = stringResource(R.string.forge_import_failed)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val parsed = withContext(Dispatchers.IO) {
                ObtainiumImportLive.readUri(context, uri)?.let { ObtainiumImportLive.applyJson(context, it) }
            }
            if (parsed == null) {
                SettingsToast.fail(context, failed)
            } else {
                SettingsToast.ok(context, context.getString(R.string.forge_import_ok, parsed.imported, parsed.skipped))
            }
        }
    }
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.forge_import_body), style = MaterialTheme.typography.bodySmall)
        TextButton(
            onClick = { picker.launch("*/*") },
            modifier = Modifier.semantics { contentDescription = pickLabel },
        ) {
            Text(pickLabel)
        }
    }
}
