package dev.foss.goldenpath.inventory

import java.io.File
import java.util.zip.ZipFile

/** ABI folders under `lib/` in an APK or split. Empty when the archive has no native libs. */
object ApkNativeZip {
    private val libAbi = Regex("""^lib/([^/]+)/""")

    fun abis(file: File): Set<String> {
        if (!ApkZip.ok(file)) return emptySet()
        return runCatching {
            ZipFile(file).use { zip ->
                zip.entries().asSequence().mapNotNull { entry ->
                    libAbi.find(entry.name.replace('\\', '/'))?.groupValues?.get(1)
                }.toSet()
            }
        }.getOrDefault(emptySet())
    }
}
