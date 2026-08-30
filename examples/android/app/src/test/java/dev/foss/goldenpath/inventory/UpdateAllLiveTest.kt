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
                            ticked.set(installs.get() == 0)
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
