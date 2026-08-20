package dev.foss.goldenpath.index.fdroid

import java.io.File

class FdroidIndexStore(private val dir: File) {
    fun load(repoId: String, nowMs: Long): ByteArray? {
        val meta = metaFile(repoId)
        val data = dataFile(repoId)
        if (!meta.isFile || !data.isFile) return null
        val fetched = meta.readText().trim().toLongOrNull() ?: return null
        if (!FdroidCachePolicy.isFresh(fetched, nowMs)) return null
        return data.readBytes()
    }

    fun save(repoId: String, bytes: ByteArray, nowMs: Long) {
        if (bytes.isEmpty()) return
        dir.mkdirs()
        dataFile(repoId).writeBytes(bytes)
        metaFile(repoId).writeText(nowMs.toString())
    }

    private fun safe(repoId: String): String =
        repoId.replace(Regex("[^A-Za-z0-9._-]"), "_")

    private fun dataFile(repoId: String) = File(dir, "${safe(repoId)}.index")

    private fun metaFile(repoId: String) = File(dir, "${safe(repoId)}.fetched")
}
