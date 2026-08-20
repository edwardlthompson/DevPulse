package dev.foss.goldenpath.index.fdroid

object FdroidApkUrl {
    fun of(repoId: String, apkName: String?): String? {
        val name = apkName?.trim().orEmpty()
        if (name.isEmpty() || '/' in name || '\\' in name || ".." in name) return null
        val base = base(repoId) ?: return null
        return base + name
    }

    fun base(repoId: String): String? = when (repoId) {
        "official" -> "https://f-droid.org/repo/"
        "archive" -> "https://f-droid.org/archive/"
        "izzy" -> "https://apt.izzysoft.de/fdroid/repo/"
        "guardian" -> "https://guardianproject.info/fdroid/repo/"
        "calyx" -> "https://calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo/"
        else -> null
    }
}
