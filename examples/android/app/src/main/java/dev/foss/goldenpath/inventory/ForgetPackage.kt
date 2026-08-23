package dev.foss.goldenpath.inventory

import java.io.File

object ForgetPackage {
    fun wipe(packageName: String, filesDir: File) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        RemoteReleaseMemory.drop(pkg)
        IgnoredUpdates.drop(pkg, filesDir)
        AppliedUpdates.clearOne(pkg)
        val notes = File(filesDir, "update_notes.tsv")
        if (notes.isFile) {
            val kept = notes.readLines().filter { !it.startsWith("$pkg\t") }
            runCatching { notes.writeText(kept.joinToString("\n")) }
        }
    }
}
