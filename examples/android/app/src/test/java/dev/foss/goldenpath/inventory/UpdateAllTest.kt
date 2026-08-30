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
        UpdateAllCancel.arm()
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
        val withoutAurora = UpdateAllPick.candidates(app, auroraPlay = false).single()
        assertEquals(RemoteReleasedSource.Fdroid, withoutAurora.source)
    }

    @Test
    fun apkMirrorIsFetchableOnlyWithCachedDownloadPhp() {
        val pkg = "app.mirror"
        assertEquals(false, UpdateAll.fetchable(RemoteReleasedSource.ApkMirror, pkg))
        UpdateArtifactMemory.add(
            UpdateArtifact(
                pkg,
                RemoteReleasedSource.ApkMirror,
                "https://www.apkmirror.com/wp-content/themes/APKMirror/download.php?id=9",
            ),
        )
        assertEquals(true, UpdateAll.fetchable(RemoteReleasedSource.ApkMirror, pkg))
    }

    @Test
    fun groupsHonorSelectedPackages() {
        val one = sampleApp("app.one", "One", remoteVersionName = "2.0")
        val two = sampleApp("app.two", "Two", remoteVersionName = "2.0")
        UpdateArtifactMemory.add(UpdateArtifact("app.one", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/a.apk"))
        UpdateArtifactMemory.add(UpdateArtifact("app.two", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/b.apk"))
        val all = UpdateAllPick.groups(listOf(one, two))
        val onlyTwo = UpdateAllPick.groups(listOf(one, two), setOf("app.two"))
        assertEquals(2, all.size)
        assertEquals(listOf("app.two"), onlyTwo.map { it.first().packageName })
    }

    @Test
    fun candidatesSkipListingAboveDeviceSdk() {
        val app = sampleApp(
            "app.high",
            "High",
            remoteVersionName = "3.0",
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Fdroid, versionName = "3.0", listed = true, minSdk = 35),
            ),
        )
        assertEquals(0, UpdateAllPick.candidates(app, deviceSdk = 34).size)
    }
}
