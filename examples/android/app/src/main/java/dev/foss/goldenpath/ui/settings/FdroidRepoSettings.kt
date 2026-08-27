package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.fdroid.FdroidCustomIndex
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@Composable
fun FdroidRepoSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { FdroidRepoPreferences(context) }
    val scope = rememberCoroutineScope()
    val saved by prefs.customIndexUrl.collectAsStateWithLifecycle("")
    var draft by remember { mutableStateOf<String?>(null) }
    val value = draft ?: saved
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.repo_help_body), style = MaterialTheme.typography.bodySmall)
        Text(text = stringResource(R.string.fdroid_signature_note), style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = value,
            onValueChange = { draft = it },
            label = { Text(stringResource(R.string.fdroid_custom_url)) },
        )
        TextButton(
            onClick = {
                scope.launch {
                    if (FdroidCustomIndex.valid(value)) {
                        prefs.setCustomIndexUrl(value)
                        SettingsToast.ok(context, context.getString(R.string.sources_saved_ok))
                    } else {
                        SettingsToast.fail(context, context.getString(R.string.sources_index_invalid))
                    }
                }
            },
        ) {
            Text(stringResource(R.string.fdroid_custom_save))
        }
    }
}

internal fun fdroidRepoTitleRes(repoId: String): Int = when (repoId) {
    "official" -> R.string.fdroid_repo_official
    "archive" -> R.string.fdroid_repo_archive
    "izzy" -> R.string.fdroid_repo_izzy
    "guardian" -> R.string.fdroid_repo_guardian
    "calyx" -> R.string.fdroid_repo_calyx
    "microg" -> R.string.fdroid_repo_microg
    "newpipe" -> R.string.fdroid_repo_newpipe
    "divest" -> R.string.fdroid_repo_divest
    "kde" -> R.string.fdroid_repo_kde
    "cromite" -> R.string.fdroid_repo_cromite
    "iode" -> R.string.fdroid_repo_iode
    else -> R.string.fdroid_custom_url
}
