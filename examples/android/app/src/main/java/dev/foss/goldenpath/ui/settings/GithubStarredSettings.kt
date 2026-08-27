package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.inventory.GithubStarredLive
import dev.foss.goldenpath.inventory.PackageManagerPackageCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GithubStarredSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val needToken = stringResource(R.string.forge_starred_need_token)
    val forbidden = stringResource(R.string.forge_starred_forbidden)
    val failed = stringResource(R.string.forge_starred_failed)
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.forge_starred_body), style = MaterialTheme.typography.bodySmall)
        TextButton(
            onClick = {
                scope.launch {
                    val token = EncryptedForgeTokenStore.wrap(context).getToken()?.trim().orEmpty()
                    if (token.isEmpty()) {
                        SettingsToast.fail(context, needToken)
                        return@launch
                    }
                    val result = withContext(Dispatchers.IO) {
                        val installed = PackageManagerPackageCatalog(context.packageManager).listInstalled()
                        GithubStarredLive.run(context, installed)
                    }
                    val msg = when (result.statusCode) {
                        401 -> needToken
                        403, 429 -> forbidden
                        in 200..299 -> context.getString(R.string.forge_starred_ok, result.matched, result.stars)
                        else -> failed
                    }
                    if (result.statusCode in 200..299) SettingsToast.ok(context, msg) else SettingsToast.fail(context, msg)
                }
            },
        ) {
            Text(stringResource(R.string.forge_starred_scan))
        }
    }
}
