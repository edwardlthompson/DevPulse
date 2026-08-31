package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class UpdateAllFallbackTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun candidatesIncludeApkPureFallbackWhenAvailable() {
        val app = sampleApp(
            "io.mapgenie.division2map",
            "MapGenie",
            remoteVersionName = "2.4.2",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Play, UpdateUrls.play("io.mapgenie.division2map"), "2.4.2", listed = true),
            ),
        ).copy(versionName = "2.4.1", versionCode = 34)

        UpdateArtifactMemory.add(
            UpdateArtifact(
                "io.mapgenie.division2map",
                RemoteReleasedSource.ApkPure,
                "https://d.apkpure.com/b/APK/io.mapgenie.division2map?versionCode=35",
                versionName = "2.4.2",
                versionCode = 35,
            ),
        )

        val candidates = UpdateAllPick.candidates(app)
        assertEquals(2, candidates.size)
        assertEquals(RemoteReleasedSource.Play, candidates[0].source)
        assertEquals(RemoteReleasedSource.ApkPure, candidates[1].source)
    }

    @Test
    fun fallsBackToSecondaryWhenPrimaryDownloadFails() {
        val app = sampleApp(
            "io.mapgenie.division2map",
            "MapGenie",
            remoteVersionName = "2.4.2",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Play, UpdateUrls.play("io.mapgenie.division2map"), "2.4.2", listed = true),
            ),
        ).copy(versionName = "2.4.1", versionCode = 34)

        UpdateArtifactMemory.add(
            UpdateArtifact(
                "io.mapgenie.division2map",
                RemoteReleasedSource.ApkPure,
                "https://d.apkpure.com/b/APK/io.mapgenie.division2map?versionCode=35",
                versionName = "2.4.2",
                versionCode = 35,
            ),
        )

        val groups = UpdateAllPick.groups(listOf(app))
        val jobs = groups.map { it.first() }
        val dummyApk = File.createTempFile("test_mapgenie", ".apk").apply { deleteOnExit() }

        val res = UpdateAll.run(
            jobs = jobs,
            groups = groups,
            prepare = { job, _ ->
                if (job.source == RemoteReleasedSource.Play) {
                    ListingFail.none()
                    null
                } else {
                    listOf(dummyApk)
                }
            },
            install = { true },
        )

        assertEquals(1, res.downloaded)
        assertEquals(1, res.installed)
        assertEquals(1, res.failedDownload)
        assertTrue(AppliedUpdates.settled("io.mapgenie.division2map"))
    }
}
