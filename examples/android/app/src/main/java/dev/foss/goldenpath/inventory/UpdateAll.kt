package dev.foss.goldenpath.inventory

import java.io.File

data class UpdateAllResult(
    val downloaded: Int,
    val installed: Int,
    val failedDownload: Int,
    val failedInstall: Int,
)

data class UpdateAllJob(
    val packageName: String,
    val label: String,
    val source: RemoteReleasedSource,
    val pageUrl: String?,
    val versionName: String? = null,
)

enum class UpdateAllPhase { Wait, Fetch, Ready, Apply, Ok, Fail }

data class UpdateAllSnap(
    val packageName: String,
    val label: String,
    val source: RemoteReleasedSource,
    val phase: UpdateAllPhase,
    val received: Long = 0,
    val expected: Long = -1,
    val failDownload: Boolean = false,
    val failWhy: InstallWhy = InstallWhy.NoFile,
    val stay: Boolean = true,
)

object UpdateAll {
    const val PARALLEL = 6
    const val SNAP_MS = 300L

    @Volatile
    private var lastSnapAt = 0L

    fun jobs(apps: List<InstalledApp>): List<UpdateAllJob> =
        UpdateAllPick.groups(apps).map { it.first() }

    fun artifacts(apps: List<InstalledApp>): List<UpdateArtifact> =
        jobs(apps).mapNotNull { job ->
            UpdateArtifactMemory.forSource(job.packageName, job.source)
                ?: UpdateArtifactMemory.best(job.packageName)
        }

    fun jobFor(app: InstalledApp): UpdateAllJob? = UpdateAllPick.candidates(app).firstOrNull()

    fun run(
        jobs: List<UpdateAllJob>,
        prepare: (UpdateAllJob, (Long, Long) -> Unit) -> List<File>?,
        install: (List<File>) -> Boolean,
        onSnap: (UpdateAllSnap) -> Unit = {},
        filesDir: File? = null,
        groups: List<List<UpdateAllJob>>? = null,
        clash: (UpdateAllJob, List<File>) -> Boolean = { _, _ -> false },
    ): UpdateAllResult {
        val startedAt = System.currentTimeMillis()
        val result = UpdateAllPipe.run(jobs, prepare, install, onSnap, filesDir, groups, clash)
        filesDir?.let {
            PulseHistory.note(
                it,
                "update",
                System.currentTimeMillis() - startedAt,
                result.installed,
                "downloaded=${result.downloaded};failDl=${result.failedDownload};failIns=${result.failedInstall}",
            )
        }
        return result
    }

    internal fun fetchable(
        source: RemoteReleasedSource,
        packageName: String = "",
        auroraPlay: Boolean = true,
    ): Boolean = when (source) {
        RemoteReleasedSource.None -> false
        RemoteReleasedSource.Play -> auroraPlay
        RemoteReleasedSource.ApkMirror -> cachedMirror(packageName)
        else -> true
    }

    private fun cachedMirror(packageName: String): Boolean {
        val url = UpdateArtifactMemory.forSource(packageName, RemoteReleasedSource.ApkMirror)
            ?.downloadUrl
            .orEmpty()
        return url.contains("download.php", ignoreCase = true)
    }

    internal fun snapBytes(read: Long, total: Long, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (read == 0L || (total > 0L && read == total)) return true
        if (nowMs - lastSnapAt < SNAP_MS) return false
        lastSnapAt = nowMs
        return true
    }
}
