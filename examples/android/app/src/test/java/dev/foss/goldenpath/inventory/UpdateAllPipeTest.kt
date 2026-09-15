package dev.foss.goldenpath.inventory

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllPipeTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun nextWaveDownloadsWhileInstallWaits() {
        val jobs = listOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null),
            UpdateAllJob("app.b", "B", RemoteReleasedSource.Izzy, null),
            UpdateAllJob("app.c", "C", RemoteReleasedSource.Guardian, null),
        )
        val order = CopyOnWriteArrayList<String>()
        val sawC = AtomicBoolean(false)
        val overlapped = AtomicBoolean(false)
        val names = java.util.concurrent.ConcurrentHashMap<File, String>()
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ ->
                order += "dl:${job.packageName}"
                if (job.packageName == "app.c") sawC.set(true)
                val file = File.createTempFile(job.packageName, ".apk")
                names[file] = job.packageName
                listOf(file)
            },
            install = { files ->
                order += "ins:${names[files.first()]}"
                val deadline = System.currentTimeMillis() + 2_000
                while (System.currentTimeMillis() < deadline) {
                    if (sawC.get()) {
                        overlapped.set(true)
                        break
                    }
                    Thread.sleep(10)
                }
                true
            },
        )
        assertEquals(3, result.downloaded)
        assertEquals(3, result.installed)
        assertTrue(overlapped.get())
    }
}
