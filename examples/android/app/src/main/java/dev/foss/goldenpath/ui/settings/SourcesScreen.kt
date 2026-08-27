package dev.foss.goldenpath.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.components.MenuOverlay

enum class SourceSetup {
    Play,
    Fdroid,
    Forge,
    Starred,
    Dump,
    Aptoide,
    Paste,
    Obtainium,
}

@Composable
fun SourcesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var setup by remember { mutableStateOf<SourceSetup?>(null) }
    val childOpen = setup != null
    BackHandler(enabled = childOpen) { setup = null }
    MenuOverlay(
        open = childOpen,
        modifier = modifier,
        parent = {
            SettingsPane(
                title = stringResource(R.string.settings_section_sources),
                onBack = onBack,
                modifier = Modifier.fillMaxSize(),
            ) { SourcesToggleList(onSetup = { setup = it }) }
        },
        child = {
            when (val open = setup) {
                null -> Unit
                else -> SettingsPane(
                    title = stringResource(titleRes(open)),
                    onBack = { setup = null },
                    modifier = Modifier.fillMaxSize(),
                ) { SetupBody(open) }
            }
        },
    )
}

@Composable
private fun SetupBody(setup: SourceSetup) {
    when (setup) {
        SourceSetup.Play -> PlayLookupSettings()
        SourceSetup.Fdroid -> FdroidRepoSettings()
        SourceSetup.Forge -> ForgeLookupSettings()
        SourceSetup.Starred -> GithubStarredSettings()
        SourceSetup.Dump -> DumpStoreLookupSettings()
        SourceSetup.Aptoide -> AptoideLookupSettings()
        SourceSetup.Paste -> ForgePasteSettings()
        SourceSetup.Obtainium -> ObtainiumImportSettings()
    }
}

private fun titleRes(setup: SourceSetup): Int = when (setup) {
    SourceSetup.Play -> R.string.play_lookup_title
    SourceSetup.Fdroid -> R.string.fdroid_repos_title
    SourceSetup.Forge -> R.string.forge_lookup_title
    SourceSetup.Starred -> R.string.forge_starred_scan
    SourceSetup.Dump -> R.string.dump_store_title
    SourceSetup.Aptoide -> R.string.aptoide_title
    SourceSetup.Paste -> R.string.forge_title
    SourceSetup.Obtainium -> R.string.forge_import
}
