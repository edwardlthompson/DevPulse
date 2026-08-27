package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Column
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
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.settings.SourceFieldValidate
import dev.foss.goldenpath.ui.forge.ForgePasteScreen
import java.io.File

@Composable
fun ForgePasteSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { FilePastedRepoStore(File(context.filesDir, "pasted_repos.tsv")) }
    var packageName by remember { mutableStateOf("") }
    var repoUrl by remember { mutableStateOf("") }
    Column(modifier = modifier) {
        ForgePasteScreen(
            packageName = packageName,
            repoUrl = repoUrl,
            onPackageNameChange = { packageName = it },
            onRepoUrlChange = { repoUrl = it },
        )
        TextButton(
            onClick = {
                if (SourceFieldValidate.paste(packageName, repoUrl)) {
                    store.put(packageName, repoUrl)
                    SettingsToast.ok(context, context.getString(R.string.sources_saved_ok))
                } else {
                    SettingsToast.fail(context, context.getString(R.string.sources_paste_invalid))
                }
            },
        ) {
            Text(stringResource(R.string.forge_paste_save))
        }
    }
}
