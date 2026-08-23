package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllRunTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun downloadsAllThenQueuesInstalls() {
        val jobs = listOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, "https://f-droid.org/packages/a/"),
            UpdateAllJob("app.b", "B", RemoteReleasedSource.Izzy, "https://apt.izzysoft.de/fdroid/index/apk/b"),
        )
        val order = mutableListOf<String>()
        val snaps = mutableListOf<UpdateAllPhase>()
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ ->
                order += "dl:${job.packageName}"
                listOf(File.createTempFile(job.packageName, ".apk"))
            },
            install = { files ->
                order += "ins:${files.first().name}"
                true
            },
            onSnap = { snaps += it.phase },
        )
        assertEquals(2, result.downloaded)
        assertEquals(2, result.installed)
        val firstIns = order.indexOfFirst { it.startsWith("ins:") }
        assertEquals(setOf("dl:app.a", "dl:app.b"), order.take(firstIns).toSet())
        assertTrue(order.drop(firstIns).all { it.startsWith("ins:") })
        assertTrue(snaps.contains(UpdateAllPhase.Ok))
    }

    @Test
    fun failedPrepareSkipsInstallAndContinues() {
        val jobs = listOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null),
            UpdateAllJob("app.b", "B", RemoteReleasedSource.Fdroid, null),
        )
        val installed = mutableListOf<String>()
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ ->
                if (job.packageName == "app.a") null else listOf(File.createTempFile("appb", ".apk"))
            },
            install = { files ->
                installed += files.first().name
                true
            },
        )
        assertEquals(1, result.downloaded)
        assertEquals(1, result.installed)
        assertEquals(1, result.failedDownload)
        assertEquals(1, installed.size)
    }

    @Test
    fun failedNewestTriesNextVersionThenIgnores() {
        val play = UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "3.0")
        val fdroid = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2.0")
        val tried = mutableListOf<String>()
        val result = UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, fdroid)),
            prepare = { job, _ ->
                tried += "${job.source.name}:${job.versionName}"
                if (job.source == RemoteReleasedSource.Play) null else listOf(File.createTempFile("appn", ".apk"))
            },
            install = { true },
        )
        assertEquals(listOf("Play:3.0", "Fdroid:2.0"), tried)
        assertEquals(1, result.downloaded)
        assertEquals(1, result.installed)
        assertTrue(IgnoredUpdates.has("app.a", RemoteReleasedSource.Play, "3.0"))
        assertFalse(IgnoredUpdates.has("app.a", RemoteReleasedSource.Fdroid, "2.0"))
        assertTrue(AppliedUpdates.settled("app.a"))
    }

    @Test
    fun cancelStopsFurtherInstalls() {
        val jobs = listOf(
            UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null),
            UpdateAllJob("app.b", "B", RemoteReleasedSource.Izzy, null),
        )
        val installed = mutableListOf<String>()
        val result = UpdateAll.run(
            jobs = jobs,
            prepare = { job, _ -> listOf(File.createTempFile(job.packageName, ".apk")) },
            install = { files ->
                installed += files.first().name
                UpdateAllCancel.request()
                true
            },
        )
        assertEquals(1, installed.size)
        assertEquals(1, result.installed)
        assertTrue(result.downloaded >= 1)
    }

    @Test
    fun cancelDoesNotIgnoreTheCurrentJob() {
        val job = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "2.0")
        UpdateAll.run(
            jobs = listOf(job),
            prepare = { _, _ -> listOf(File.createTempFile("appa", ".apk")) },
            install = {
                UpdateAllCancel.request()
                false
            },
        )
        assertFalse(IgnoredUpdates.has("app.a", RemoteReleasedSource.Fdroid, "2.0"))
    }
}
