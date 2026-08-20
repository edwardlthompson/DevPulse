package dev.foss.goldenpath.inventory

import java.io.File

fun interface ApkBytesFetcher {
    fun get(url: String): Result<ByteArray>
}

object ApkFileStore {
    fun fileFor(dir: File, artifact: UpdateArtifact): File {
        val code = artifact.versionCode?.toString() ?: "latest"
        val safe = artifact.packageName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(dir, "$safe-$code.apk")
    }

    fun write(file: File, bytes: ByteArray): File {
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file
    }

    fun save(dir: File, artifact: UpdateArtifact, fetcher: ApkBytesFetcher): Result<File> {
        val url = ApkDownloadUrl.httpsFile(artifact.downloadUrl) ?: return Result.failure(IllegalArgumentException("url"))
        return fetcher.get(url).map { bytes ->
            if (bytes.isEmpty()) error("empty apk")
            write(fileFor(dir, artifact), bytes)
        }
    }
}
