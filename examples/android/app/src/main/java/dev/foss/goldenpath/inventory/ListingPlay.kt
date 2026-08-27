package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aurora.AuroraPlayFile
import java.io.File

object ListingPlay {
    fun download(
        cacheDir: File,
        packageName: String,
        parts: List<AuroraPlayFile>,
        save: (String, File, (Long, Long) -> Unit) -> Boolean,
        inspect: (File) -> ApkInspect,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): List<File>? {
        var offset = 0L
        return writeFiles(cacheDir, packageName, parts, inspect) { url, dest ->
            val ok = save(url, dest) { read, total ->
                onProgress(offset + read, if (total > 0) offset + total else -1L)
            }
            if (ok && dest.isFile) offset += dest.length()
            ok
        }
    }

    fun write(
        cacheDir: File,
        packageName: String,
        parts: List<AuroraPlayFile>,
        bytesFor: (String) -> ByteArray?,
        inspect: (File) -> ApkInspect,
    ): List<File>? = writeFiles(cacheDir, packageName, parts, inspect) { url, dest ->
        val bytes = bytesFor(url) ?: return@writeFiles false
        if (bytes.isEmpty()) return@writeFiles false
        ApkFileStore.write(dest, bytes)
        true
    }

    fun writeFiles(
        cacheDir: File,
        packageName: String,
        parts: List<AuroraPlayFile>,
        inspect: (File) -> ApkInspect,
        save: (String, File) -> Boolean,
    ): List<File>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || parts.isEmpty()) return null
        val safe = pkg.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val out = ArrayList<File>(parts.size)
        for ((index, part) in parts.withIndex()) {
            val dest = File(cacheDir, "$safe-${part.versionCode ?: 0}-$index.apk")
            if (!save(part.url, dest) || !dest.isFile || dest.length() == 0L) {
                dest.delete()
                out.forEach { it.delete() }
                return null
            }
            val name = inspect(dest).packageName
            val keep = name == pkg || (name == null && zipMagic(dest))
            if (!keep) {
                dest.delete()
                continue
            }
            out += dest
        }
        if (out.isEmpty()) return null
        return out
    }

    private fun zipMagic(file: File): Boolean = file.inputStream().use { input ->
        input.read() == 0x50 && input.read() == 0x4B
    }
}
