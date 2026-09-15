package dev.foss.goldenpath.inventory

import java.io.File
import java.util.zip.ZipFile

/** Turns an APKPure XAPK/ZIP into the matching base APK plus splits. */
object XapkUnpack {
    fun expand(
        archive: File,
        packageName: String,
        destDir: File,
        inspect: (File) -> ApkInspect,
    ): List<File>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || !ApkZip.ok(archive)) return null
        val out = File(destDir, "${pkg}.xapk.d")
        out.deleteRecursively()
        if (!out.mkdirs()) return null
        val kept = mutableListOf<File>()
        var matched = false
        val ok = runCatching {
            ZipFile(archive).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.isDirectory) continue
                    val base = entry.name.replace('\\', '/').substringAfterLast('/')
                    if (base.isEmpty() || base.contains("..") || !base.endsWith(".apk", ignoreCase = true)) continue
                    val dest = File(out, base)
                    if (dest.exists() || dest.canonicalFile.parentFile != out.canonicalFile) continue
                    zip.getInputStream(entry).use { input -> dest.outputStream().use { input.copyTo(it) } }
                    val got = inspect(dest).packageName
                    if (got == pkg) matched = true
                    if (got == null || got == pkg) kept += dest else dest.delete()
                }
            }
        }.isSuccess
        if (!ok || !matched || kept.isEmpty()) {
            out.deleteRecursively()
            return null
        }
        return kept
    }
}
