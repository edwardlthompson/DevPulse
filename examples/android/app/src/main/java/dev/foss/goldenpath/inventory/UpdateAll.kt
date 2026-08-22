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

enum class UpdateAllPhase { Wait, Fetch, Apply, Ok, Fail }

data class UpdateAllSnap(
    val packageName: String,
    val label: String,
    val source: RemoteReleasedSource,
    val phase: UpdateAllPhase,
    val received: Long = 0,
    val expected: Long = -1,
    val failDownload: Boolean = false,
    val stay: Boolean = true,
)

object UpdateAll {
    const val MAX_FILES = 40
    const val PARALLEL = 4

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
    ): UpdateAllResult {
        val startedAt = System.currentTimeMillis()
        val counts = intArrayOf(0, 0, 0, 0)
        val open = (groups ?: jobs.map { listOf(it) }).map { group ->
            (group.firstOrNull()?.packageName.orEmpty()) to group.toMutableList()
        }
        val settled = mutableSetOf<String>()
        while (true) {
            val wave = open.mapNotNull { (pkg, group) ->
                if (pkg.isEmpty() || pkg in settled) {
                    group.clear()
                    null
                } else {
                    group.firstOrNull { !IgnoredUpdates.has(it.packageName, it.source, it.versionName) }
                }
            }
            if (wave.isEmpty()) break
            val fetched = ReleaseRefreshParallel.map(wave, PARALLEL) { job ->
                RefreshTrace.line("update all try ${job.source.name} ${job.packageName} ${job.versionName}")
                onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Fetch))
                job to prepare(job) { read, total ->
                    onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Fetch, read, total))
                }
            }
            val ready = UpdateAllQueue.takeDownloads(fetched, open, filesDir, onSnap, counts)
            UpdateAllQueue.installReady(ready, open, settled, install, filesDir, onSnap, counts)
        }
        val result = UpdateAllResult(counts[0], counts[1], counts[2], counts[3])
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

    internal fun fetchable(source: RemoteReleasedSource): Boolean = when (source) {
        RemoteReleasedSource.None, RemoteReleasedSource.ApkMirror -> false
        else -> true
    }
}
