package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    val playOn by prefs.playLookupEnabled.collectAsStateWithLifecycle(true)
    val auroraOn by prefs.auroraPlayEnabled.collectAsStateWithLifecycle(true)
    val forgeOn by prefs.forgeLookupEnabled.collectAsStateWithLifecycle(true)
    val aptoideOn by prefs.aptoideLookupEnabled.collectAsStateWithLifecycle(false)
    val apkMirrorOn by prefs.apkMirrorLookupEnabled.collectAsStateWithLifecycle(false)
    val apkPureOn by prefs.apkPureLookupEnabled.collectAsStateWithLifecycle(false)
    val defaultRepos = remember { FdroidRepoCatalog.defaults() }
    val repoStates = defaultRepos.associate { repo ->
        repo.id to repos.repoEnabled(repo.id).collectAsStateWithLifecycle(repo.enabled).value
    }
    val allSelected = playOn && auroraOn && forgeOn && aptoideOn && apkMirrorOn && apkPureOn && repoStates.values.all { it }

    SettingsGroup(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.sources_select_all),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(
                onClick = {
                    val target = !allSelected
                    scope.launch {
                        prefs.setPlayLookupEnabled(target)
                        prefs.setAuroraPlayEnabled(target)
                        prefs.setForgeLookupEnabled(target)
                        prefs.setAptoideLookupEnabled(target)
                        prefs.setApkMirrorLookupEnabled(target)
                        prefs.setApkPureLookupEnabled(target)
                        defaultRepos.forEach { repo ->
                            repos.setRepoEnabled(repo.id, target)
                        }
                        if (target && SourceToggleGate.allowOn(SourceToggleId.CustomFdroid, gate)) {
                            repos.setCustomEnabled(true)
                        } else if (!target) {
                            repos.setCustomEnabled(false)
                        }
                        if (target && SourceToggleGate.allowOn(SourceToggleId.SearchUnknowns, gate)) {
                            prefs.setForgeLookupSearchUnknowns(true)
                        } else if (!target) {
                            prefs.setForgeLookupSearchUnknowns(false)
                        }
                        if (target && SourceToggleGate.allowOn(SourceToggleId.Starred, gate)) {
                            starred.setEnabled(true)
                        } else if (!target) {
                            starred.setEnabled(false)
                        }
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        if (allSelected) R.string.sources_deselect_all else R.string.sources_select_all_action,
                    ),
                )
            }
        }
        HorizontalDivider()
        SourceSwitch(
            label = stringResource(R.string.play_lookup_enable),
            checked = playOn,
            setup = SourceSetup.Play,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setPlayLookupEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.aurora_play_enable),
            checked = auroraOn,
            setup = SourceSetup.Play,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setAuroraPlayEnabled(it) } },
        )
        defaultRepos.forEach { repo ->
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
            checked = forgeOn,
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
            checked = aptoideOn,
            setup = SourceSetup.Aptoide,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setAptoideLookupEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.apkmirror_enable),
            checked = apkMirrorOn,
            setup = SourceSetup.Dump,
            onSetup = onSetup,
            onCheckedChange = { scope.launch { prefs.setApkMirrorLookupEnabled(it) } },
        )
        SourceSwitch(
            label = stringResource(R.string.apkpure_enable),
            checked = apkPureOn,
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
