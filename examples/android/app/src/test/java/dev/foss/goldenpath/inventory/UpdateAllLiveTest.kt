package dev.foss.goldenpath.inventory

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllLiveTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun downloadOkTicksBeforeInstallsStart() {
        val jobs = listOf(
            UpdateAllJob("fast", "F", RemoteReleasedSource.Fdroid, null),
            UpdateAllJob("slow", "S", RemoteReleasedSource.Izzy, null),
        )
        val snaps = CopyOnWriteArrayList<UpdateAllSnap>()
        val installs = AtomicInteger(0)
        val ticked = AtomicBoolean(false)
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ ->
                if (job.packageName == "slow") {
                    val deadline = System.currentTimeMillis() + 3_000
                    while (System.currentTimeMillis() < deadline) {
                        if (snaps.any { it.packageName == "fast" && it.phase == UpdateAllPhase.Ready }) {
                            ticked.set(true)
                            break
                        }
                        Thread.sleep(10)
                    }
                }
                listOf(File.createTempFile(job.packageName, ".apk"))
            },
            install = {
                installs.incrementAndGet()
                true
            },
            onSnap = { snaps += it },
        )
        assertEquals(2, result.downloaded)
        assertEquals(2, result.installed)
        assertTrue(ticked.get())
    }

    @Test
    fun installsReadyBatchBeforeNextWave() {
        val jobs = listOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null),
            UpdateAllJob("app.b", "B", RemoteReleasedSource.Izzy, null),
            UpdateAllJob("app.c", "C", RemoteReleasedSource.Guardian, null),
        )
        val order = CopyOnWriteArrayList<String>()
        val names = java.util.concurrent.ConcurrentHashMap<File, String>()
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ ->
                order += "dl:${job.packageName}"
                val file = File.createTempFile(job.packageName, ".apk")
                names[file] = job.packageName
                listOf(file)
            },
            install = { files ->
                order += "ins:${names[files.first()]}"
                true
            },
        )
        assertEquals(3, result.downloaded)
        assertEquals(3, result.installed)
        assertEquals(3, order.count { it.startsWith("dl:") })
        assertEquals(3, order.count { it.startsWith("ins:") })
    }

    @Test
    fun slowDownloadDoesNotBlockLaterPrepare() {
        val jobs = (0..6).map { i ->
            UpdateAllJob("app.$i", "$i", RemoteReleasedSource.Fdroid, null)
        }
        val started = java.util.concurrent.ConcurrentHashMap<String, Long>()
        val t0 = System.nanoTime()
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ ->
                started[job.packageName] = System.nanoTime() - t0
                if (job.packageName == "app.0") Thread.sleep(1_200)
                listOf(File.createTempFile(job.packageName, ".apk"))
            },
            install = { true },
        )
        assertEquals(7, result.downloaded)
        assertEquals(7, result.installed)
        val late = started["app.6"] ?: error("app.6 never prepared")
        val slow = started["app.0"] ?: error("app.0 never prepared")
        assertTrue(late < 1_200_000_000L)
        assertTrue(late >= slow)
    }

    @Test
    fun prepareThrowCountsAsFailedDownload() {
        val result = UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.a", "A", RemoteReleasedSource.Forge, null)),
            prepare = { _, _ -> error("Unable to resolve host") },
            install = { true },
        )
        assertEquals(0, result.downloaded)
        assertEquals(1, result.failedDownload)
        assertEquals(0, result.installed)
    }
}
