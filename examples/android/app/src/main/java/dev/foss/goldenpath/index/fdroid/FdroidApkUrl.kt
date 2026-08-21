package dev.foss.goldenpath.index.fdroid

object FdroidApkUrl {
    fun of(repoId: String, apkName: String?): String? {
        val name = apkName?.trim().orEmpty()
        if (name.isEmpty() || '/' in name || '\\' in name || ".." in name) return null
        val base = FdroidRepoCatalog.apkBase(repoId) ?: return null
        return base + name
    }
}
