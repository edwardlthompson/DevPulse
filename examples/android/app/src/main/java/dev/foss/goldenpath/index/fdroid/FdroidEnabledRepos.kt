package dev.foss.goldenpath.index.fdroid

import kotlinx.coroutines.flow.first

object FdroidEnabledRepos {
    suspend fun list(prefs: FdroidRepoPreferences): List<FdroidRepo> {
        val custom = prefs.customIndexUrl.first().trim()
        val defaults = FdroidRepoCatalog.defaults().filter { repo ->
            prefs.repoEnabled(repo.id).first()
        }
        if (custom.isEmpty()) return defaults
        return defaults + FdroidRepo(
            id = "custom",
            kind = FdroidRepoKind.Custom,
            indexUrl = custom,
            enabled = true,
        )
    }
}
