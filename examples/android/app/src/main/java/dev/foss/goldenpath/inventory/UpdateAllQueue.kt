package dev.foss.goldenpath.inventory

import java.io.File

/** Download-wave bookkeeping, then a sequential install queue. counts is dl, ins, failDl, failIns. */
internal object UpdateAllQueue {
    fun takeDownloads(
        fetched: List<UpdateAllFetched>,
        open: List<Pair<String, MutableList<UpdateAllJob>>>,
        filesDir: File?,
        onSnap: (UpdateAllSnap) -> Unit,
        counts: IntArray,
    ): List<Pair<UpdateAllJob, List<File>>> {
        val ready = mutableListOf<Pair<UpdateAllJob, List<File>>>()
        fetched.forEach { item ->
            val job = item.job
            val group = groupOf(open, job.packageName)
            group.removeAll { it.source == job.source && it.versionName == job.versionName }
            if (item.files.isNullOrEmpty()) {
                counts[2] += 1
                if (item.why == InstallWhy.PlayPurchase) group.clear()
                val why = if (
                    item.why == InstallWhy.NoFile &&
                    job.source == RemoteReleasedSource.Play &&
                    !more(group)
                ) {
                    InstallWhy.PlayStore
                } else {
                    item.why
                }
                if (filesDir != null && item.why != InstallWhy.PlayPurchase) {
                    IgnoredUpdates.add(job.packageName, job.source, job.versionName, filesDir)
                }
                fail(job, filesDir, onSnap, more(group), download = true, why = why)
            } else {
                counts[0] += 1
                onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Ready))
                ready += job to item.files
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
        clash: (UpdateAllJob, List<File>) -> Boolean = { _, _ -> false },
    ) {
        ready.forEach { (job, files) ->
            if (UpdateAllCancel.requested()) return
            if (job.packageName in settled) return@forEach
            if (clash(job, files)) {
                RefreshTrace.line("update all signing ${job.packageName}")
                SignerReplaceQueue.remember(filesDir, job, files)
                fail(job, filesDir, onSnap, more(groupOf(open, job.packageName)), download = false, why = InstallWhy.Signing)
                if (UpdateAllCancel.requested()) return
                return@forEach
            }
            onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Apply))
            val ok = install(files)
            if (ok) {
                counts[1] += 1
                settled += job.packageName
                AppliedUpdates.settle(job.packageName, job.versionName, filesDir = filesDir)
                SignerReplaceQueue.drop(filesDir, job.packageName)
                filesDir?.let { UpdateAllLog.note(it, job, "ok", "") }
                onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Ok, stay = false))
                groupOf(open, job.packageName).clear()
            } else if (!UpdateAllCancel.requested()) {
                counts[3] += 1
                fail(job, filesDir, onSnap, more(groupOf(open, job.packageName)), download = false)
            }
            if (UpdateAllCancel.requested()) return
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
        why: InstallWhy = InstallWhy.Permission,
    ) {
        filesDir?.let { UpdateAllLog.note(it, job, if (download) "failDl" else "failIns", why.name) }
        onSnap(
            UpdateAllSnap(
                job.packageName,
                job.label,
                job.source,
                UpdateAllPhase.Fail,
                failDownload = download,
                failWhy = why,
                stay = more,
            ),
        )
    }
}
