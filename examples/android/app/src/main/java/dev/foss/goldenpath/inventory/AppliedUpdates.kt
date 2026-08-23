package dev.foss.goldenpath.inventory

/** Packages that received a successful install this session. Cleared on Refresh. */
object AppliedUpdates {
    @Volatile
    private var settled: Set<String> = emptySet()

    fun settle(packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        synchronized(this) { settled = settled + pkg }
        InstalledAppsRevision.bump()
    }

    fun settled(packageName: String): Boolean = packageName.trim() in settled

    fun clearOne(packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        synchronized(this) { settled = settled - pkg }
        InstalledAppsRevision.bump()
    }

    fun clear() {
        synchronized(this) { settled = emptySet() }
        InstalledAppsRevision.bump()
    }
}
