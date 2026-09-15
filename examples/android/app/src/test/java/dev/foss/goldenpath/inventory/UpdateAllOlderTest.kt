package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllOlderTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun olderListingIsIgnoredSilentlyAndLeavesUpdates() {
        val dir = File.createTempFile("uaolder", "dir").apply { delete(); mkdirs() }
        val snaps = mutableListOf<UpdateAllSnap>()
        val result = UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.g", "G", RemoteReleasedSource.Play, null, "20.0")),
            prepare = { _, _ -> ListingFail.older() },
            install = { false },
            filesDir = dir,
            onSnap = { snaps += it },
        )
        assertTrue(IgnoredUpdates.has("app.g", RemoteReleasedSource.Play, "20.0"))
        assertEquals(0, result.failedDownload)
        assertTrue(snaps.none { it.phase == UpdateAllPhase.Fail })
        val app = sampleApp(
            "app.g",
            "G",
            remoteVersionName = "20.0",
            remoteReleasedSource = RemoteReleasedSource.Play,
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Play, versionName = "20.0", listed = true),
            ),
        )
        assertFalse(UpdateInventory.hasUpdate(app))
        dir.deleteRecursively()
    }

    @Test
    fun olderDoesNotTryApkPure() {
        val play = UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "3.0")
        val apkpure = UpdateAllJob("app.a", "A", RemoteReleasedSource.ApkPure, null, "3.0")
        val tried = mutableListOf<String>()
        val result = UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, apkpure)),
            prepare = { job, _ ->
                tried += job.source.name
                ListingFail.older()
            },
            install = { false },
        )
        assertEquals(listOf("Play"), tried)
        assertEquals(0, result.failedDownload)
        assertEquals(0, result.downloaded)
    }

    @Test
    fun olderKeepsANewerFallbackOnUpdates() {
        val dir = File.createTempFile("uaolder2", "dir").apply { delete(); mkdirs() }
        val play = UpdateAllJob("app.a", "A", RemoteReleasedSource.Play, null, "3.0")
        val fdroid = UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null, "4.0")
        val apk = File.createTempFile("okapk", ".apk")
        val result = UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, fdroid)),
            prepare = { job, _ ->
                if (job.source == RemoteReleasedSource.Play) ListingFail.older() else listOf(apk)
            },
            install = { true },
            filesDir = dir,
        )
        assertTrue(IgnoredUpdates.has("app.a", RemoteReleasedSource.Play, "3.0"))
        assertEquals(1, result.downloaded)
        assertEquals(0, result.failedDownload)
        assertEquals(1, result.installed)
        apk.delete()
        dir.deleteRecursively()
    }

    @Test
    fun sdkIsIgnoredSilently() {
        val dir = File.createTempFile("uasdk", "dir").apply { delete(); mkdirs() }
        val result = UpdateAll.run(
            jobs = listOf(UpdateAllJob("app.x86", "X", RemoteReleasedSource.Aptoide, null, "2.0")),
            prepare = { _, _ -> ListingFail.sdk() },
            install = { false },
            filesDir = dir,
        )
        assertTrue(IgnoredUpdates.has("app.x86", RemoteReleasedSource.Aptoide, "2.0"))
        assertEquals(0, result.failedDownload)
        dir.deleteRecursively()
    }

    @Test
    fun olderLogsSkipNotFail() {
        val lines = mutableListOf<String>()
        RefreshTrace.emit = { lines += it }
        try {
            UpdateAll.run(
                jobs = listOf(UpdateAllJob("app.g", "G", RemoteReleasedSource.Play, null, "20.0")),
                prepare = { _, _ -> ListingFail.older() },
                install = { false },
            )
        } finally {
            RefreshTrace.emit = {}
        }
        assertTrue(lines.any { it.contains("dl skip Older") })
        assertFalse(lines.any { it.contains("dl fail Older") })
    }
}
