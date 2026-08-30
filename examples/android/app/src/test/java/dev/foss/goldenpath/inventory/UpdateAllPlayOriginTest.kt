package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAllPlayOriginTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun playInstalledAppsDoNotSideloadAptoide() {
        val app = sampleApp(
            "com.twitter.android",
            "X",
            remoteVersionName = "100.0.0",
            origin = AppOrigin.Play,
            latestListings = listOf(
                UpdateLink(RemoteReleasedSource.Aptoide, versionName = "100.0.0", listed = true),
                UpdateLink(RemoteReleasedSource.Play, UpdateUrls.play("com.twitter.android"), "10.0", listed = true),
            ),
        )
        val jobs = UpdateAllPick.candidates(app)
        assertEquals(listOf(RemoteReleasedSource.Play), jobs.map { it.source })
    }

    @Test
    fun playInstalledAppsDoNotOfferUnlistedNonPlayRemoteVersionSource() {
        val app = sampleApp(
            "com.twitter.android",
            "X",
            remoteVersionName = "2.0",
            origin = AppOrigin.Play,
            latestListings = emptyList(),
        ).copy(remoteVersionSource = RemoteReleasedSource.ApkPure)
        assertEquals(false, UpdateInventory.hasUpdate(app))
        assertEquals(emptyList<UpdateAllJob>(), UpdateAllPick.candidates(app))
    }

    @Test
    fun playEmptyWithNoNextSourceOpensPlayStoreWhy() {
        val play = UpdateAllJob("com.android.chrome", "Chrome", RemoteReleasedSource.Play, null, "152.0")
        val snaps = mutableListOf<UpdateAllSnap>()
        val result = UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play)),
            prepare = { _, _ ->
                ListingFail.why = InstallWhy.NoFile
                null
            },
            install = { true },
            onSnap = { snaps += it },
        )
        assertEquals(0, result.downloaded)
        assertEquals(1, result.failedDownload)
        assertEquals(InstallWhy.PlayStore, snaps.last { it.phase == UpdateAllPhase.Fail }.failWhy)
        assertTrue(snaps.last { it.phase == UpdateAllPhase.Fail }.stay.not())
    }
}
