package dev.foss.goldenpath.inventory

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.Future

/** Keeps up to PARALLEL downloads in flight; installs stay one at a time. */
internal object UpdateAllPipe {
    fun run(
        jobs: List<UpdateAllJob>,
        prepare: (UpdateAllJob, (Long, Long) -> Unit) -> List<File>?,
        install: (List<File>) -> Boolean,
        onSnap: (UpdateAllSnap) -> Unit,
        filesDir: File?,
        groups: List<List<UpdateAllJob>>?,
        clash: (UpdateAllJob, List<File>) -> Boolean,
    ): UpdateAllResult {
        val counts = intArrayOf(0, 0, 0, 0)
        val open = (groups ?: jobs.map { listOf(it) }).map { group ->
            (group.firstOrNull()?.packageName.orEmpty()) to group.toMutableList()
        }
        val settled = ConcurrentHashMap.newKeySet<String>()
        val held = ConcurrentHashMap.newKeySet<String>()
        val downloading = ConcurrentHashMap.newKeySet<String>()
        val lock = Any()
        val installLock = Any()
        val pool = Executors.newSingleThreadExecutor()
        val downloadPool = Executors.newFixedThreadPool(UpdateAll.PARALLEL)
        val installs = CopyOnWriteArrayList<Future<*>>()
        val downloads = CopyOnWriteArrayList<Future<*>>()
        UpdateAllResume.checkpoint(filesDir, open, settled)
        try {
            while (!UpdateAllCancel.requested()) {
                val wave = next(open, settled, held, downloading)
                wave.forEach { job ->
                    downloading += job.packageName
                    downloads += downloadPool.submit {
                        try {
                            fetch(job, prepare, onSnap, open, filesDir, counts, held, lock, ready = { batch ->
                                if (batch.isNotEmpty()) {
                                    installs += pool.submit {
                                        synchronized(installLock) {
                                            UpdateAllQueue.installReady(
                                                batch, open, settled, install, filesDir, onSnap, counts, clash,
                                            )
                                            held.removeAll(batch.map { it.first.packageName }.toSet())
                                            UpdateAllResume.checkpoint(filesDir, open, settled)
                                        }
                                    }
                                }
                            })
                        } finally {
                            downloading -= job.packageName
                        }
                    }
                }
                val pending = (downloads + installs).filter { !it.isDone }
                if (wave.isEmpty() && pending.isEmpty()) break
                if (pending.isNotEmpty()) UpdateAllSlots.waitDone(pending)
                UpdateAllResume.checkpoint(filesDir, open, settled)
            }
            (downloads + installs).forEach { runCatching { it.get() } }
        } finally {
            downloadPool.shutdown()
            pool.shutdown()
            downloadPool.awaitTermination(30, java.util.concurrent.TimeUnit.MINUTES)
            pool.awaitTermination(30, java.util.concurrent.TimeUnit.MINUTES)
        }
        return UpdateAllResult(counts[0], counts[1], counts[2], counts[3])
    }

    private fun next(
        open: List<Pair<String, MutableList<UpdateAllJob>>>,
        settled: Set<String>,
        held: Set<String>,
        downloading: Set<String>,
    ): List<UpdateAllJob> {
        val room = (UpdateAll.PARALLEL - downloading.size).coerceAtLeast(0)
        if (room == 0) return emptyList()
        return open.mapNotNull { (pkg, group) ->
            if (pkg.isEmpty() || pkg in settled) {
                group.clear()
                null
            } else if (pkg in held || pkg in downloading) {
                null
            } else {
                group.firstOrNull { !IgnoredUpdates.has(it.packageName, it.source, it.versionName) }
            }
        }.take(room)
    }

    private fun fetch(
        job: UpdateAllJob,
        prepare: (UpdateAllJob, (Long, Long) -> Unit) -> List<File>?,
        onSnap: (UpdateAllSnap) -> Unit,
        open: List<Pair<String, MutableList<UpdateAllJob>>>,
        filesDir: File?,
        counts: IntArray,
        held: MutableSet<String>,
        lock: Any,
        ready: (List<Pair<UpdateAllJob, List<File>>>) -> Unit,
    ): UpdateAllFetched {
        if (UpdateAllCancel.requested()) return UpdateAllFetched(job, null)
        RefreshTrace.line("update all try ${job.source.name} ${job.packageName} ${job.versionName}")
        onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Fetch))
        ListingFail.why = InstallWhy.NoFile
        val files = runCatching {
            prepare(job) { read, total ->
                if (UpdateAll.snapBytes(read, total)) {
                    onSnap(UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Fetch, read, total))
                }
            }
        }.getOrElse {
            RefreshTrace.line("update all dl error ${job.source.name} ${job.packageName} ${it.message}")
            ListingFail.none()
        }
        val item = UpdateAllFetched(job, files, ListingFail.why)
        RefreshTrace.line(
            "update all dl ${UpdateAllSkip.dlLine(!files.isNullOrEmpty(), item.why)} ${job.source.name} ${job.packageName}",
        )
        synchronized(lock) {
            val batch = UpdateAllQueue.takeDownloads(listOf(item), open, filesDir, onSnap, counts)
            if (batch.isNotEmpty()) held += job.packageName
            ready(batch)
        }
        return item
    }
}
