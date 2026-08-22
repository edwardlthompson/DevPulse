package dev.foss.goldenpath.inventory

import java.io.File

/** Download-wave bookkeeping, then a sequential install queue. counts is dl, ins, failDl, failIns. */
internal object UpdateAllQueue {
    fun takeDownloads(
        fetched: List<Pair<UpdateAllJob, List<File>?>>,
        open: List<Pair<String, MutableList<UpdateAllJob>>>,
        filesDir: File?,
        onSnap: (UpdateAllSnap) -> Unit,
        counts: IntArray,
    ): List<Pair<UpdateAllJob, List<File>>> {
        val ready = mutableListOf<Pair<UpdateAllJob, List<File>>>()
        fetched.forEach { (job, files) ->
            val group = groupOf(open, job.packageName)
            group.removeAll { it.source == job.source && it.versionName == job.versionName }
            if (files.isNullOrEmpty()) {
                counts[2] += 1
                fail(job, filesDir, onSnap, more(group), download = true)
            } else {
                counts[0] += 1
                ready += job to files
            }
        }
        return ready
    }

    fun installReady(
        ready: List<Pair<UpdateAllJob, List<File>>>,
        open: List<Pair<String, MutableList<UpdateAllJob>>>,
        settled: MutableSet<String>,
        install: (List<File>) -> Boolean,
        filesDir: File?,
        onSnap: (UpdateAllSnap) -> Unit,
        counts: IntArray,
    ) {
        ready.forEach { (job, files) ->
            if (job.packageName in settled) return@forEach
            onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Apply))
            if (install(files)) {
                counts[1] += 1
                settled += job.packageName
                AppliedUpdates.settle(job.packageName)
                onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Ok, stay = false))
                groupOf(open, job.packageName).clear()
            } else {
                counts[3] += 1
                fail(job, filesDir, onSnap, more(groupOf(open, job.packageName)), download = false)
            }
        }
    }

    private fun more(group: List<UpdateAllJob>): Boolean =
        group.any { !IgnoredUpdates.has(it.packageName, it.source, it.versionName) }

    private fun groupOf(
        open: List<Pair<String, MutableList<UpdateAllJob>>>,
        packageName: String,
    ): MutableList<UpdateAllJob> = open.firstOrNull { it.first == packageName }?.second ?: mutableListOf()

    private fun fail(
        job: UpdateAllJob,
        filesDir: File?,
        onSnap: (UpdateAllSnap) -> Unit,
        more: Boolean,
        download: Boolean,
    ) {
        IgnoredUpdates.add(job.packageName, job.source, job.versionName, filesDir)
        onSnap(
            UpdateAllSnap(
                job.packageName,
                job.label,
                job.source,
                UpdateAllPhase.Fail,
                failDownload = download,
                stay = more,
            ),
        )
    }
}
