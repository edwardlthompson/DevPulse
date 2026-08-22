package dev.foss.goldenpath.inventory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpdateArtifact(
    val packageName: String,
    val source: RemoteReleasedSource,
    val downloadUrl: String,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val sha256: String? = null,
    val localPath: String? = null,
    val nativeCodes: Set<String> = emptySet(),
)

object UpdateArtifactRank {
    fun rank(source: RemoteReleasedSource): Int = when (source) {
        RemoteReleasedSource.Fdroid -> 0
        RemoteReleasedSource.Archive -> 1
        RemoteReleasedSource.Izzy -> 2
        RemoteReleasedSource.Guardian -> 3
        RemoteReleasedSource.Calyx -> 4
        RemoteReleasedSource.ExtraRepo -> 5
        RemoteReleasedSource.Forge -> 6
        RemoteReleasedSource.Aptoide -> 7
        RemoteReleasedSource.ApkPure -> 8
        RemoteReleasedSource.ApkMirror, RemoteReleasedSource.Play, RemoteReleasedSource.None -> 99
    }
}

object UpdateArtifactMemory {
    @Volatile
    var byPackage: Map<String, List<UpdateArtifact>> = emptyMap()
        private set

    private val lock = Any()
    private val revisionState = MutableStateFlow(0)
    val revision: StateFlow<Int> = revisionState.asStateFlow()

    fun add(artifact: UpdateArtifact) {
        val pkg = artifact.packageName.trim()
        val url = ApkDownloadUrl.httpsFile(artifact.downloadUrl) ?: return
        if (pkg.isEmpty()) return
        val clean = artifact.copy(packageName = pkg, downloadUrl = url)
        synchronized(lock) {
            val next = (byPackage[pkg].orEmpty() + clean).distinctBy { it.source to it.downloadUrl }
            byPackage = byPackage + (pkg to next)
            revisionState.value += 1
        }
    }

    fun best(packageName: String): UpdateArtifact? =
        byPackage[packageName]?.minByOrNull { UpdateArtifactRank.rank(it.source) }

    fun forSource(packageName: String, source: RemoteReleasedSource): UpdateArtifact? =
        byPackage[packageName.trim()]?.firstOrNull { it.source == source }

    fun markLocal(packageName: String, source: RemoteReleasedSource, path: String) {
        synchronized(lock) {
            val list = byPackage[packageName] ?: return
            byPackage = byPackage + (packageName to list.map {
                if (it.source == source) it.copy(localPath = path) else it
            })
            revisionState.value += 1
        }
    }

    fun clear() {
        synchronized(lock) {
            byPackage = emptyMap()
            revisionState.value += 1
        }
    }
}
