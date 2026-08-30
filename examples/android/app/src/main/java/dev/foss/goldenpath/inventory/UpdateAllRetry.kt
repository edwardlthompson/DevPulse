package dev.foss.goldenpath.inventory

import java.io.File

object UpdateAllRetry {
    fun downloads(filesDir: File): Int = retry(filesDir, setOf("failDl"))

    fun installs(filesDir: File): Int = retry(filesDir, setOf("failIns"))

    fun fails(filesDir: File): Int = retry(filesDir, setOf("failDl", "failIns"))

    fun ignored(filesDir: File): Int {
        val n = IgnoredUpdates.rows.size
        IgnoredUpdates.clearPersisted(filesDir)
        InstalledAppsRevision.bump()
        return n
    }

    internal fun retry(filesDir: File, results: Set<String>): Int {
        val file = UpdateAllLog.file(filesDir)
        val rows = UpdateAllLog.load(file)
        val hit = rows.filter { it.result in results }
        if (hit.isEmpty()) return 0
        hit.forEach { IgnoredUpdates.drop(it.packageName, filesDir) }
        UpdateAllLog.replace(file, rows.filterNot { it.result in results })
        InstalledAppsRevision.bump()
        return hit.size
    }
}
