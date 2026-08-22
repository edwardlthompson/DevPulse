package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aurora.AuroraPlayFile
import java.io.File

object ListingPlay {
    fun download(
        cacheDir: File,
        packageName: String,
        parts: List<AuroraPlayFile>,
        fetch: (String, (Long, Long) -> Unit) -> Result<ByteArray>,
        inspect: (File) -> ApkInspect,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): List<File>? {
        var offset = 0L
        return write(cacheDir, packageName, parts, bytesFor = { url ->
            fetch(url) { read, total ->
                onProgress(offset + read, if (total > 0) offset + total else -1L)
            }.getOrNull()?.also { offset += it.size }
        }, inspect)
    }

    fun write(
        cacheDir: File,
        packageName: String,
        parts: List<AuroraPlayFile>,
        bytesFor: (String) -> ByteArray?,
        inspect: (File) -> ApkInspect,
    ): List<File>? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || parts.isEmpty()) return null
        val safe = pkg.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val out = ArrayList<File>(parts.size)
        for ((index, part) in parts.withIndex()) {
            val bytes = bytesFor(part.url) ?: continue
            if (bytes.isEmpty()) continue
            val file = ApkFileStore.write(
                File(cacheDir, "$safe-${part.versionCode ?: 0}-$index.apk"),
                bytes,
            )
            val name = inspect(file).packageName
            val keep = name == pkg || (name == null && bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte())
            if (!keep) {
                file.delete()
                continue
            }
            out += file
        }
        return out.takeIf { it.isNotEmpty() }
    }
}
