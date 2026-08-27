package dev.foss.goldenpath.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.FileWatchedRepoStore
import dev.foss.goldenpath.index.forge.ObtainiumImport
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ObtainiumImportSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var json by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val label = stringResource(R.string.forge_import)
    val pickLabel = stringResource(R.string.forge_import_file)
    val failed = stringResource(R.string.forge_import_failed)
    fun apply(raw: String?) {
        val parsed = raw?.let { runCatching { ObtainiumImport.parse(it) }.getOrNull() }
        if (parsed == null || (parsed.imported == 0 && parsed.skipped == 0)) {
            message = failed
            return
        }
        val files = context.filesDir
        ObtainiumImport.persist(
            parsed.rows,
            FilePastedRepoStore(File(files, "pasted_repos.tsv")),
            FileGithubVerifiedStore(File(files, "github_verified.tsv")),
            FileWatchedRepoStore(File(files, "github_watched.tsv")),
        )
        message = context.getString(R.string.forge_import_ok, parsed.imported, parsed.skipped)
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val raw = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { ObtainiumImport.readUtf8(it) }
            }
            apply(raw)
        }
    }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = json,
            onValueChange = { json = it },
            modifier = Modifier.semantics { contentDescription = label },
            label = { Text(label) },
        )
        TextButton(onClick = { apply(json) }) { Text(label) }
        TextButton(
            onClick = { picker.launch(arrayOf("application/json", "text/*", "*/*")) },
            modifier = Modifier.semantics { contentDescription = pickLabel },
        ) { Text(pickLabel) }
        message?.let { Text(it) }
    }
}
