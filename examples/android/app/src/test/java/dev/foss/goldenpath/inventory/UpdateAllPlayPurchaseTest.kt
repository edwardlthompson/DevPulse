package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class UpdateAllPlayPurchaseTest {
    @Before
    fun reset() {
        IgnoredUpdates.clear()
        AppliedUpdates.clear()
        UpdateAllCancel.arm()
    }

    @Test
    fun playPurchaseDoesNotFallThroughToTheNextSource() {
        val play = UpdateAllJob("app.paid", "Paid", RemoteReleasedSource.Play, null, "3.0")
        val fdroid = UpdateAllJob("app.paid", "Paid", RemoteReleasedSource.Fdroid, null, "2.0")
        val tried = mutableListOf<String>()
        val snaps = mutableListOf<UpdateAllSnap>()
        val result = UpdateAll.run(
            jobs = listOf(play),
            groups = listOf(listOf(play, fdroid)),
            prepare = { job, _ ->
                tried += job.source.name
                ListingFail.why = InstallWhy.PlayPurchase
                null
            },
            install = { true },
            onSnap = { snaps += it },
        )
        assertEquals(listOf("Play"), tried)
        assertEquals(0, result.downloaded)
        assertEquals(1, result.failedDownload)
        assertEquals(InstallWhy.PlayPurchase, snaps.last { it.phase == UpdateAllPhase.Fail }.failWhy)
        assertFalse(IgnoredUpdates.has("app.paid", RemoteReleasedSource.Play, "3.0"))
    }
}
