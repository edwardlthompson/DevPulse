package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.fdroid.FdroidRepoCatalog
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.inventory.GithubStarredPrefs
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.settings.SourceGateState
import dev.foss.goldenpath.settings.SourceToggleGate
import dev.foss.goldenpath.settings.SourceToggleId
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@Composable
fun SourcesToggleList(onSetup: (SourceSetup) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val repos = remember { FdroidRepoPreferences(context) }
    val starred = remember { GithubStarredPrefs(context) }
    val scope = rememberCoroutineScope()
    val tokenPresent = remember { EncryptedForgeTokenStore.wrap(context).getToken() != null }
    val customUrl by repos.customIndexUrl.collectAsStateWithLifecycle("")
    val gate = SourceGateState(tokenPresent, customUrl)
    SettingsGroup(modifier = modifier) {
        SourceSwitch(
            label = stringResource(R.string.play_lookup_enable),
            checked = prefs.playLookupEnabled.collectAsStateWithLifecycle(true).value,
            setup = SourceSetup.Play,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setPlayLookupEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.aurora_play_enable),
            checked = prefs.auroraPlayEnabled.collectAsStateWithLifecycle(false).value,
            setup = SourceSetup.Play,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setAuroraPlayEnabled(it) } },
        )
        FdroidRepoCatalog.defaults().forEach { repo ->
            val on by repos.repoEnabled(repo.id).collectAsStateWithLifecycle(repo.enabled)
            SourceSwitch(
                label = stringResource(fdroidRepoTitleRes(repo.id)),
                checked = on,
                setup = SourceSetup.Fdroid,
                onSetup = onSetup,
                onCheckedChange = { value -> scope.launch { repos.setRepoEnabled(repo.id, value) } },
            )
        }
        GatedSwitch(
            label = stringResource(R.string.sources_custom_enable),
            checked = repos.customEnabled.collectAsStateWithLifecycle(false).value,
            id = SourceToggleId.CustomFdroid,
            gate = gate,
            blocked = stringResource(R.string.sources_need_index),
            setup = SourceSetup.Fdroid,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { repos.setCustomEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.forge_lookup_enable),
            checked = prefs.forgeLookupEnabled.collectAsStateWithLifecycle(true).value,
            setup = SourceSetup.Forge,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setForgeLookupEnabled(it) } },
        )
        GatedSwitch(
            label = stringResource(R.string.forge_lookup_search_unknowns),
            checked = prefs.forgeLookupSearchUnknowns.collectAsStateWithLifecycle(false).value,
            id = SourceToggleId.SearchUnknowns,
            gate = gate,
            blocked = stringResource(R.string.sources_need_token),
            setup = SourceSetup.Forge,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setForgeLookupSearchUnknowns(it) } },
        )
        GatedSwitch(
            label = stringResource(R.string.forge_starred_scan),
            checked = starred.enabled.collectAsStateWithLifecycle(false).value,
            id = SourceToggleId.Starred,
            gate = gate,
            blocked = stringResource(R.string.sources_need_token),
            setup = SourceSetup.Starred,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { starred.setEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.aptoide_enable),
            checked = prefs.aptoideLookupEnabled.collectAsStateWithLifecycle(false).value,
            setup = SourceSetup.Aptoide,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setAptoideLookupEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.apkmirror_enable),
            checked = prefs.apkMirrorLookupEnabled.collectAsStateWithLifecycle(false).value,
            setup = SourceSetup.Dump,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setApkMirrorLookupEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.apkpure_enable),
            checked = prefs.apkPureLookupEnabled.collectAsStateWithLifecycle(false).value,
            setup = SourceSetup.Dump,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setApkPureLookupEnabled(it) } },
        )
        SetupOnly(stringResource(R.string.forge_paste_save), SourceSetup.Paste, onSetup)
        SetupOnly(stringResource(R.string.forge_import), SourceSetup.Obtainium, onSetup)
    }
}

@Composable
private fun GatedSwitch(
    label: String,
    checked: Boolean,
    id: SourceToggleId,
    gate: SourceGateState,
    blocked: String,
    setup: SourceSetup,
    onSetup: (SourceSetup) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    SourceSwitch(label, checked, setup, onSetup) { on ->
        if (on && !SourceToggleGate.allowOn(id, gate)) {
            SettingsToast.fail(context, blocked)
        } else {
            onCheckedChange(on)
        }
    }
}

@Composable
private fun SourceSwitch(
    label: String,
    checked: Boolean,
    setup: SourceSetup,
    onSetup: (SourceSetup) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    val setupLabel = stringResource(R.string.sources_setup)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "$label. $setupLabel" }
                .clickable(role = Role.Button) { onSetup(setup) },
        ) {
            Text(text = label)
            Text(text = setupLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}

@Composable
private fun SetupOnly(label: String, setup: SourceSetup, onSetup: (SourceSetup) -> Unit) {
    val setupLabel = stringResource(R.string.sources_setup)
    Column(
        modifier = Modifier
            .semantics { contentDescription = "$label. $setupLabel" }
            .clickable(role = Role.Button) { onSetup(setup) },
    ) {
        Text(text = label)
        Text(text = setupLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}
