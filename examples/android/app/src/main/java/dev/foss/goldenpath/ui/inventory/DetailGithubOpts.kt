package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import dev.foss.goldenpath.index.forge.FileGithubAppOptStore
import dev.foss.goldenpath.index.forge.GithubAppOpt
import dev.foss.goldenpath.index.forge.GithubAppOptCodec
import dev.foss.goldenpath.ui.settings.PreferenceSwitch
import java.io.File

@Composable
fun DetailGithubOpts(packageName: String) {
    val context = LocalContext.current
    val store = remember { FileGithubAppOptStore(File(context.filesDir, "github_app_opts.tsv")) }
    val saved = remember(packageName) { store.get(packageName) ?: GithubAppOpt() }
    var prerelease by remember(packageName) { mutableStateOf(saved.includePrereleases) }
    var regex by remember(packageName) { mutableStateOf(saved.apkRegex.orEmpty()) }
    val regexLabel = stringResource(R.string.forge_opt_apk_regex)
    Column {
        PreferenceSwitch(
            label = stringResource(R.string.forge_opt_prerelease),
            checked = prerelease,
            onCheckedChange = {
                prerelease = it
                store.put(packageName, GithubAppOpt(it, GithubAppOptCodec.regexOrNull(regex)?.pattern))
            },
        )
        OutlinedTextField(
            value = regex,
            onValueChange = { value ->
                regex = value
                val clean = GithubAppOptCodec.regexOrNull(value)?.pattern
                store.put(packageName, GithubAppOpt(prerelease, clean))
            },
            modifier = Modifier.semantics { contentDescription = regexLabel },
            label = { Text(regexLabel) },
            singleLine = true,
        )
    }
}
