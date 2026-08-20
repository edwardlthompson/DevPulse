package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.AppOrigin

object FdroidOrigin {
    fun from(kind: FdroidRepoKind): AppOrigin = when (kind) {
        FdroidRepoKind.Official -> AppOrigin.Fdroid
        else -> AppOrigin.ExtraRepo
    }
}

object FdroidRepoCatalog {
    fun defaults(): List<FdroidRepo> = listOf(
        FdroidRepo(
            id = "official",
            kind = FdroidRepoKind.Official,
            indexUrl = "https://f-droid.org/repo/index-v1.jar",
            enabled = true,
        ),
        FdroidRepo(
            id = "archive",
            kind = FdroidRepoKind.Archive,
            indexUrl = "https://f-droid.org/archive/index-v1.jar",
            enabled = false,
        ),
        FdroidRepo(
            id = "izzy",
            kind = FdroidRepoKind.Izzy,
            indexUrl = "https://apt.izzysoft.de/fdroid/repo/index-v1.jar",
            enabled = false,
        ),
        FdroidRepo(
            id = "guardian",
            kind = FdroidRepoKind.Guardian,
            indexUrl = "https://guardianproject.info/fdroid/repo/index-v1.jar",
            enabled = false,
        ),
        FdroidRepo(
            id = "calyx",
            kind = FdroidRepoKind.Calyx,
            indexUrl = "https://calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo/index-v1.jar",
            enabled = false,
        ),
    )
}
