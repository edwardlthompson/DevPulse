package dev.foss.goldenpath.inventory

object VersionDelta {
    fun line(installed: String?, newest: String?, installedCode: Long = 0): String? {
        val have = installed?.trim().orEmpty()
        val want = newest?.trim().orEmpty()
        if (have.isEmpty() || want.isEmpty()) return null
        if (!VersionCompare.isNewer(want, have, installedCode)) return null
        return "$have → $want"
    }

    fun newest(app: InstalledApp): String? {
        if (!UpdateInventory.hasUpdate(app)) return null
        val listed = UpdateInventory.usable(app).mapNotNull { it.versionName?.trim()?.takeIf(String::isNotEmpty) }
        return listed.maxWithOrNull { left, right -> VersionCompare.compare(left, right) }
            ?: app.remoteVersionName?.trim()?.takeIf(String::isNotEmpty)
    }
}
