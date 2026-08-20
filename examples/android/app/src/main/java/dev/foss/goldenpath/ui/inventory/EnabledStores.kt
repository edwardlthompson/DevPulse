package dev.foss.goldenpath.ui.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.index.fdroid.FdroidRepoPreferences
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.StoreSelection

@Composable
fun rememberEnabledSources(): Set<RemoteReleasedSource> {
    val context = LocalContext.current
    val inventory = remember { InventoryPreferences(context) }
    val repos = remember { FdroidRepoPreferences(context) }
    val play by inventory.playLookupEnabled.collectAsStateWithLifecycle(true)
    val aptoide by inventory.aptoideLookupEnabled.collectAsStateWithLifecycle(false)
    val forge by inventory.forgeLookupEnabled.collectAsStateWithLifecycle(true)
    val official by repos.repoEnabled("official").collectAsStateWithLifecycle(true)
    val archive by repos.repoEnabled("archive").collectAsStateWithLifecycle(false)
    val izzy by repos.repoEnabled("izzy").collectAsStateWithLifecycle(false)
    val guardian by repos.repoEnabled("guardian").collectAsStateWithLifecycle(false)
    val calyx by repos.repoEnabled("calyx").collectAsStateWithLifecycle(false)
    val custom by repos.customIndexUrl.collectAsStateWithLifecycle("")
    return remember(play, aptoide, forge, official, archive, izzy, guardian, calyx, custom) {
        StoreSelection.sources(
            play = play,
            aptoide = aptoide,
            forge = forge,
            repoIds = buildSet {
                if (official) add("official")
                if (archive) add("archive")
                if (izzy) add("izzy")
                if (guardian) add("guardian")
                if (calyx) add("calyx")
                if (custom.isNotBlank()) add("custom")
            },
        )
    }
}
