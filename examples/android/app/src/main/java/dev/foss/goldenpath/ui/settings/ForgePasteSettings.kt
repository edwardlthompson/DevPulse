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
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.ui.forge.ForgePasteScreen
import java.io.File

@Composable
fun ForgePasteSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { FilePastedRepoStore(File(context.filesDir, "pasted_repos.tsv")) }
    val tokenStore = remember { EncryptedForgeTokenStore.wrap(context) }
    var packageName by remember { mutableStateOf("") }
    var repoUrl by remember { mutableStateOf("") }
    var token by remember { mutableStateOf(tokenStore.getToken().orEmpty()) }
    Column(modifier = modifier) {
        ForgePasteScreen(
            packageName = packageName,
            repoUrl = repoUrl,
            token = token,
            onPackageNameChange = { packageName = it },
            onRepoUrlChange = { repoUrl = it },
            onTokenChange = { value ->
                token = value
                tokenStore.setToken(value)
            },
        )
        TextButton(onClick = { store.put(packageName, repoUrl) }) {
            Text(stringResource(R.string.forge_paste_save))
        }
    }
}
