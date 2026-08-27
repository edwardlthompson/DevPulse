package dev.foss.goldenpath.inventory

import java.io.File

/** Writes a listing APK when the archive package matches. Signer may differ (Play vs sideload). */
object ListingDownload {
    fun write(
        cacheDir: File,
        artifact: UpdateArtifact,
        bytes: ByteArray,
        inspect: (File) -> ApkInspect,
    ): File? {
        if (bytes.isEmpty()) return null
        return keep(ApkFileStore.write(ApkFileStore.fileFor(cacheDir, artifact), bytes), artifact, inspect)
    }

    fun keep(
        file: File,
        artifact: UpdateArtifact,
        inspect: (File) -> ApkInspect,
    ): File? {
        if (inspect(file).packageName != artifact.packageName) {
            file.delete()
            return null
        }
        UpdateArtifactMemory.add(artifact)
        UpdateArtifactMemory.markLocal(artifact.packageName, artifact.source, file.absolutePath)
        return file
    }
}
