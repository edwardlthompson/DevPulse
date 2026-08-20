package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.fdroid.FdroidRepoCatalog
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@Composable
fun FdroidRepoSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { FdroidRepoPreferences(context) }
    val scope = rememberCoroutineScope()
    val customUrl by prefs.customIndexUrl.collectAsStateWithLifecycle("")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.fdroid_repos_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.fdroid_signature_note), style = MaterialTheme.typography.bodySmall)
        FdroidRepoCatalog.defaults().forEach { repo ->
            RepoToggle(repoId = repo.id, kind = repo.kind, defaultOn = repo.enabled, prefs = prefs)
        }
        OutlinedTextField(
            value = customUrl,
            onValueChange = { value -> scope.launch { prefs.setCustomIndexUrl(value) } },
            label = { Text(stringResource(R.string.fdroid_custom_url)) },
        )
    }
}

@Composable
private fun RepoToggle(
    repoId: String,
    kind: FdroidRepoKind,
    defaultOn: Boolean,
    prefs: FdroidRepoPreferences,
) {
    val scope = rememberCoroutineScope()
    val enabled by prefs.repoEnabled(repoId).collectAsStateWithLifecycle(defaultOn)
    val label = stringResource(labelRes(kind))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = { value -> scope.launch { prefs.setRepoEnabled(repoId, value) } },
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}

private fun labelRes(kind: FdroidRepoKind): Int = when (kind) {
    FdroidRepoKind.Official -> R.string.fdroid_repo_official
    FdroidRepoKind.Archive -> R.string.fdroid_repo_archive
    FdroidRepoKind.Izzy -> R.string.fdroid_repo_izzy
    FdroidRepoKind.Guardian -> R.string.fdroid_repo_guardian
    FdroidRepoKind.Calyx -> R.string.fdroid_repo_calyx
    FdroidRepoKind.Custom -> R.string.fdroid_custom_url
}
