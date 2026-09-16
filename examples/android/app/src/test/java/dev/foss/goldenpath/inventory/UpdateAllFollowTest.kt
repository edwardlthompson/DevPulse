package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAllFollowTest {
    @Test
    fun magnetUntilUserScrollsThenLocks() {
        assertTrue(UpdateAllFollow.magnet(userScrolled = false))
        assertFalse(UpdateAllFollow.magnet(userScrolled = true))
        assertFalse(UpdateAllFollow.magnet(userScrolled = true))
    }

    @Test
    fun scanUpdateCtaVisibleWhenCompleteWithWork() {
        assertFalse(ScanUpdateCta.visible(complete = false, count = 3))
        assertFalse(ScanUpdateCta.visible(complete = true, count = 0))
        assertTrue(ScanUpdateCta.visible(complete = true, count = 3))
        // Predicate only — PulseRunHost no longer auto-kicks Update All.
        assertTrue(ScanUpdateCta.autoStart(complete = true, count = 3))
    }

    @Test
    fun hideWhenIdleAfterFinishedWork() {
        val done = UpdateAllSnap("app.a", "A", RemoteReleasedSource.Forge, UpdateAllPhase.Ok)
        val wait = done.copy(phase = UpdateAllPhase.Wait)
        assertFalse(
            ScanUpdateCta.hideWhenIdle(
                lookupDone = true,
                refreshing = false,
                busy = false,
                snaps = listOf(wait),
            ),
        )
        assertTrue(
            ScanUpdateCta.hideWhenIdle(
                lookupDone = true,
                refreshing = false,
                busy = false,
                snaps = listOf(done),
            ),
        )
        assertTrue(
            ScanUpdateCta.hideWhenIdle(
                lookupDone = true,
                refreshing = false,
                busy = false,
                snaps = listOf(done.copy(phase = UpdateAllPhase.Fail)),
            ),
        )
    }
}
