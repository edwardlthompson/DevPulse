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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import java.io.File

@Composable
fun DetailPasteRepo(packageName: String) {
    val context = LocalContext.current
    val store = remember { FilePastedRepoStore(File(context.filesDir, "pasted_repos.tsv")) }
    var url by remember(packageName) { mutableStateOf(store.load()[packageName].orEmpty()) }
    Column {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text(stringResource(R.string.forge_paste_url)) },
        )
        TextButton(onClick = { store.put(packageName, url) }) {
            Text(stringResource(R.string.forge_paste_save))
        }
    }
}
