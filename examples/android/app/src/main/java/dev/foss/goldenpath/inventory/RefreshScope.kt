package dev.foss.goldenpath.inventory

object RefreshScope {
    const val EXTRA_PACKAGES = "refresh_packages"

    fun apps(all: List<InstalledApp>, wanted: Collection<String>): List<InstalledApp> {
        val names = wanted.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        if (names.isEmpty()) return all
        return all.filter { it.packageName in names }
    }

    fun names(packages: Collection<String>): ArrayList<String> =
        ArrayList(packages.map { it.trim() }.filter { it.isNotEmpty() })
}
