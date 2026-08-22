package dev.foss.goldenpath.index.aurora

import dev.foss.goldenpath.inventory.ApkDownloadUrl
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory

data class AuroraPlayFile(
    val url: String,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val base: Boolean = true,
)

fun interface AuroraPlayFiles {
    fun files(packageName: String): List<AuroraPlayFile>
}

/** Maps Aurora/gplayapi file URLs onto UpdateArtifact. No live Play in unit tests. */
object AuroraPlayDirect {
    fun resolve(packageName: String, files: AuroraPlayFiles): UpdateArtifact? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        remembered(pkg)?.let { return it }
        val usable = files.files(pkg).filter { ApkDownloadUrl.httpsFile(it.url) != null }
        val file = usable.firstOrNull { it.base } ?: usable.firstOrNull() ?: return null
        val url = ApkDownloadUrl.httpsFile(file.url) ?: return null
        val artifact = UpdateArtifact(
            packageName = pkg,
            source = RemoteReleasedSource.Play,
            downloadUrl = url,
            versionName = file.versionName,
            versionCode = file.versionCode,
        )
        UpdateArtifactMemory.add(artifact)
        return artifact
    }

    private fun remembered(packageName: String): UpdateArtifact? =
        UpdateArtifactMemory.byPackage[packageName]
            ?.firstOrNull { it.source == RemoteReleasedSource.Play }
}
