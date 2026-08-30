package dev.foss.goldenpath.inventory

import java.io.File

data class AppliedUpdate(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
)

/** Packages that received a successful install. Cleared when a newer listing appears. */
object AppliedUpdates {
    const val FILE = "applied_updates.tsv"

    @Volatile
    private var rows: Map<String, AppliedUpdate> = emptyMap()
    private val lock = Any()
    private var persist: File? = null

    fun file(filesDir: File): File = File(filesDir, FILE)

    fun hydrate(filesDir: File) {
        synchronized(lock) {
            persist = file(filesDir)
            rows = load(persist!!)
        }
        InstalledAppsRevision.bump()
    }

    fun settle(
        packageName: String,
        versionName: String? = null,
        versionCode: Long = 0,
        filesDir: File? = null,
    ) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        val row = AppliedUpdate(pkg, versionName?.trim().orEmpty(), versionCode)
        synchronized(lock) {
            rows = rows + (pkg to row)
            if (filesDir != null) persist = file(filesDir)
            persist?.let { save(it, rows) }
        }
        InstalledAppsRevision.bump()
    }

    fun settled(packageName: String): Boolean = packageName.trim() in rows

    fun hides(app: InstalledApp): Boolean {
        val snap = rows[app.packageName.trim()] ?: return false
        if (snap.versionName.isEmpty() && snap.versionCode <= 0L) return true
        if (VersionCompare.isNewer(app.remoteVersionName, snap.versionName, snap.versionCode)) return false
        return app.latestListings.none { link ->
            link.listed && VersionCompare.isNewer(link.versionName, snap.versionName, snap.versionCode)
        }
    }

    fun clearOne(packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        synchronized(lock) {
            if (pkg !in rows) return
            rows = rows - pkg
            persist?.let { save(it, rows) }
        }
        InstalledAppsRevision.bump()
    }

    fun clear() {
        synchronized(lock) {
            rows = emptyMap()
            persist = null
        }
        InstalledAppsRevision.bump()
    }

    internal fun load(file: File): Map<String, AppliedUpdate> {
        if (!file.isFile) return emptyMap()
        return runCatching {
            file.readLines().mapNotNull(::parse).associateBy { it.packageName }
        }.getOrDefault(emptyMap())
    }

    internal fun parse(line: String): AppliedUpdate? {
        val parts = line.split('\t')
        if (parts.isEmpty()) return null
        val pkg = parts[0].trim()
        if (pkg.isEmpty()) return null
        val ver = parts.getOrNull(1)?.trim().orEmpty()
        val code = parts.getOrNull(2)?.trim()?.toLongOrNull() ?: 0L
        return AppliedUpdate(pkg, ver, code)
    }

    private fun save(file: File, rows: Map<String, AppliedUpdate>) {
        file.parentFile?.mkdirs()
        runCatching {
            file.writeText(
                rows.values.joinToString("\n") { "${it.packageName}\t${it.versionName}\t${it.versionCode}" },
            )
        }
    }
}
