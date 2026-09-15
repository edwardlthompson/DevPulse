package dev.foss.goldenpath.inventory

import java.io.File

object UpdateArtifactStore {
    const val FILE = "update_artifacts.tsv"

    @Volatile
    private var persist: File? = null

    fun file(filesDir: File): File = File(filesDir, FILE)

    fun hydrate(filesDir: File) {
        persist = file(filesDir)
        UpdateArtifactMemory.replaceAll(load(persist!!))
    }

    fun saveFromMemory() {
        val dest = persist ?: return
        dest.parentFile?.mkdirs()
        val rows = UpdateArtifactMemory.byPackage.values.flatten()
        runCatching { dest.writeText(rows.joinToString("\n") { encode(it) }) }
    }

    fun wipe(filesDir: File) {
        persist = null
        UpdateArtifactMemory.clear()
        file(filesDir).delete()
    }

    internal fun load(file: File): List<UpdateArtifact> {
        if (!file.isFile) return emptyList()
        return runCatching { file.readLines().mapNotNull(::parse) }.getOrDefault(emptyList())
    }

    internal fun parse(line: String): UpdateArtifact? {
        val parts = line.split('\t')
        if (parts.size < 3) return null
        val source = runCatching { RemoteReleasedSource.valueOf(parts[1].trim()) }.getOrNull() ?: return null
        val pkg = parts[0].trim()
        val url = ApkDownloadUrl.httpsFile(parts[2]) ?: return null
        if (pkg.isEmpty() || source == RemoteReleasedSource.None) return null
        return UpdateArtifact(
            packageName = pkg,
            source = source,
            downloadUrl = url,
            versionName = parts.getOrNull(3)?.trim()?.ifEmpty { null },
            versionCode = parts.getOrNull(4)?.toLongOrNull()?.takeIf { it > 0L },
            nativeCodes = parts.getOrNull(5).orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
        )
    }

    internal fun encode(artifact: UpdateArtifact): String = listOf(
        artifact.packageName,
        artifact.source.name,
        artifact.downloadUrl,
        artifact.versionName.orEmpty(),
        artifact.versionCode?.takeIf { it > 0L }?.toString().orEmpty(),
        artifact.nativeCodes.sorted().joinToString(","),
    ).joinToString("\t")
}
