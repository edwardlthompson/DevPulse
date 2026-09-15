package dev.foss.goldenpath.inventory

import java.io.File

/** Drops a downloaded listing when the archive is older or the ABI cannot run here. */
object ListingApkGuard {
    fun keep(
        files: List<File>?,
        installedCode: Long,
        deviceAbis: Set<String>,
        archiveCode: (File) -> Long,
        natives: (File) -> Set<String> = { ApkNativeZip.abis(it) },
    ): List<File>? {
        if (files.isNullOrEmpty()) return files
        val want = files.maxOf { archiveCode(it) }
        if (want > 0L && installedCode > 0L && want <= installedCode) {
            files.forEach { it.delete() }
            return ListingFail.older()
        }
        val libs = files.flatMap { natives(it) }.toSet()
        if (!ListingFit.abiOk(libs, deviceAbis)) {
            files.forEach { it.delete() }
            return ListingFail.sdk()
        }
        return files
    }
}
