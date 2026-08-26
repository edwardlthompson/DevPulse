package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.FileDirectApkStore
import java.io.File

@Composable
fun DetailDirectApk(packageName: String) {
    val context = LocalContext.current
    val store = remember { FileDirectApkStore(File(context.filesDir, "direct_apks.tsv")) }
    var url by remember(packageName) { mutableStateOf(store.load()[packageName].orEmpty()) }
    val label = stringResource(R.string.forge_direct_apk)
    Column {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.semantics { contentDescription = label },
            label = { Text(label) },
        )
        TextButton(onClick = { store.put(packageName, url) }) {
            Text(stringResource(R.string.forge_paste_save))
        }
    }
}
