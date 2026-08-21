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
        repo("official", FdroidRepoKind.Official, "https://f-droid.org/repo/", enabled = true),
        repo("archive", FdroidRepoKind.Archive, "https://f-droid.org/archive/"),
        repo("izzy", FdroidRepoKind.Izzy, "https://apt.izzysoft.de/fdroid/repo/"),
        repo("guardian", FdroidRepoKind.Guardian, "https://guardianproject.info/fdroid/repo/"),
        repo("calyx", FdroidRepoKind.Calyx, "https://calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo/"),
        repo("microg", FdroidRepoKind.Vendor, "https://microg.org/fdroid/repo/"),
        repo("newpipe", FdroidRepoKind.Vendor, "https://archive.newpipe.net/fdroid/repo/"),
        repo("divest", FdroidRepoKind.Vendor, "https://divestos.org/fdroid/official/"),
        repo("kde", FdroidRepoKind.Vendor, "https://cdn.kde.org/android/stable-releases/fdroid/repo/"),
        repo("cromite", FdroidRepoKind.Vendor, "https://www.cromite.org/fdroid/repo/"),
        repo("iode", FdroidRepoKind.Vendor, "https://fdroid.iode.tech/repo/"),
    )

    fun apkBase(repoId: String): String? {
        val index = defaults().firstOrNull { it.id == repoId }?.indexUrl ?: return null
        return index.substringBeforeLast('/') + "/"
    }

    private fun repo(id: String, kind: FdroidRepoKind, base: String, enabled: Boolean = false): FdroidRepo =
        FdroidRepo(id, kind, base.trimEnd('/') + "/index-v1.jar", enabled)
}
