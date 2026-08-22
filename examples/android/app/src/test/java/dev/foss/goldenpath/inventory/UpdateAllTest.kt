package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateAllTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
    }

    @Test
    fun queueIsDirectArtifactsForNewerApps() {
        UpdateArtifactMemory.add(
            UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.one_2.apk"),
        )
        val newer = sampleApp("app.one", "One", remoteVersionName = "2.0")
        val same = sampleApp("app.same", "Same", remoteVersionName = "1.0")
        assertEquals(1, UpdateAll.artifacts(listOf(newer, same)).size)
        assertEquals("app.one", UpdateAll.artifacts(listOf(newer)).single().packageName)
    }

    @Test
    fun jobsPickNewestFetchableListing() {
        val app = sampleApp(
            "app.play",
            "Play",
            remoteVersionName = "3.0",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Fdroid, "https://f-droid.org/packages/app.play/", "2.0", listed = true),
                UpdateLink(RemoteReleasedSource.Play, UpdateUrls.play("app.play"), "3.0", listed = true),
                UpdateLink(RemoteReleasedSource.ApkMirror, "https://www.apkmirror.com/apk/a/", "3.1", listed = true),
            ),
        )
        val job = UpdateAll.jobs(listOf(app)).single()
        assertEquals(RemoteReleasedSource.Play, job.source)
        assertEquals("app.play", job.packageName)
    }
}
