package dev.foss.goldenpath.inventory

import java.io.File

object UpdateCache {
    const val MAX_FILES = 8
    const val MAX_BYTES = 400L * 1024 * 1024

    fun stage(
        dir: File,
        artifact: UpdateArtifact,
        bytes: ByteArray,
        inspect: (File) -> ApkInspect,
        installed: InstalledIdentity,
        maxFiles: Int = MAX_FILES,
    ): Result<File> = runCatching {
        if (bytes.isEmpty()) error("empty apk")
        val sha = ApkIdentity.digest(bytes)
        if (!ApkIdentity.hashesMatch(artifact.sha256, sha)) error("sha256")
        evict(dir, maxFiles = maxFiles)
        val file = ApkFileStore.write(ApkFileStore.fileFor(dir, artifact), bytes)
        val info = inspect(file)
        if (!ApkIdentity.identityReady(artifact, info, installed)) {
            file.delete()
            error("identity")
        }
        UpdateArtifactMemory.add(artifact)
        UpdateArtifactMemory.markLocal(artifact.packageName, artifact.source, file.absolutePath)
        file
    }

    fun evict(dir: File, maxFiles: Int = MAX_FILES, maxBytes: Long = MAX_BYTES) {
        val files = dir.listFiles()?.filter { it.isFile && it.name.endsWith(".apk") }
            ?.sortedBy { it.lastModified() }
            .orEmpty()
            .toMutableList()
        var bytes = files.sumOf { it.length() }
        while (files.isNotEmpty() && (files.size >= maxFiles || bytes > maxBytes)) {
            val gone = files.removeAt(0)
            bytes -= gone.length()
            gone.delete()
        }
    }
}
