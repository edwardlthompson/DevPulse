package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshOutletBoardTest {
    @Before
    fun reset() {
        RefreshSkip.reset()
        RefreshOutletBoard.reset()
        RefreshPaceBook.clear()
    }

    @Test
    fun ticksAndEtaFromLiveRate() {
        RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", 10, nowMs = 1_000L)
        repeat(2) { RefreshOutletBoard.tick(RefreshOutletIds.PLAY) }
        val snap = RefreshOutletBoard.snaps(3_000L).single()
        assertEquals(2, snap.done)
        assertEquals(10, snap.total)
        assertEquals(8_000L, snap.etaMs)
    }

    @Test
    fun githubEtaUsesThirtyPerMinute() {
        RefreshOutletBoard.plan(RefreshOutletIds.GITHUB, "GitHub", 388, nowMs = 1L)
        val snap = RefreshOutletBoard.snaps(2L).single()
        assertEquals(388 * 60_000L / 30, snap.etaMs)
    }

    @Test
    fun stopMarksOutlet() {
        RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", 3)
        RefreshSkip.stop(RefreshOutletIds.PLAY)
        assertTrue(RefreshOutletBoard.snaps().single().skipped)
    }

    @Test
    fun pastScanEtaBeforeFirstTick() {
        RefreshPaceBook.note(RefreshOutletIds.PLAY, 10, 20_000L)
        RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", 10, nowMs = 1L)
        assertEquals(20_000L, RefreshOutletBoard.snaps(1L).single().etaMs)
    }

    @Test
    fun etaLabelMinutes() {
        assertEquals("2m 3s", RefreshOutletEta.label(123_000L))
        assertEquals("4s", RefreshOutletEta.label(4_000L))
        assertEquals("240ms", RefreshOutletEta.label(240L))
        assertEquals("0ms", RefreshOutletEta.label(0L))
    }

    @Test
    fun lastFinishedStaysOnTopFirstFinishedAtBottom() {
        val play = RefreshOutletSnap(RefreshOutletIds.PLAY, "Play", 10, 10, "", false, 0L, 1, 10)
        val github = RefreshOutletSnap(RefreshOutletIds.GITHUB, "GitHub", 10, 10, "", false, 0L, 2, 20)
        val running = RefreshOutletSnap(RefreshOutletIds.MIRROR, "APKMirror", 1, 10, "", false, 8_000L)
        assertEquals(
            listOf(RefreshOutletIds.GITHUB, RefreshOutletIds.PLAY, RefreshOutletIds.MIRROR),
            RefreshOutletEta.sorted(listOf(play, running, github)).map { it.id },
        )
    }

    @Test
    fun planDoesNotResetExistingRow() {
        RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", 2, nowMs = 1_000L)
        RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", 2, nowMs = 8_000L)
        RefreshOutletBoard.fill(RefreshOutletIds.PLAY, nowMs = 4_000L)
        assertEquals(3_000L, RefreshOutletBoard.snaps(4_000L).single().elapsedMs)
    }

    @Test
    fun finishedElapsedDoesNotKeepGrowing() {
        RefreshOutletBoard.plan(RefreshOutletIds.PLAY, "Play", 2, nowMs = 1_000L)
        RefreshOutletBoard.fill(RefreshOutletIds.PLAY, nowMs = 4_000L)
        assertEquals(3_000L, RefreshOutletBoard.snaps(4_000L).single().elapsedMs)
        assertEquals(3_000L, RefreshOutletBoard.snaps(20_000L).single().elapsedMs)
    }

    @Test
    fun resizeThenFillUsesAppCount() {
        RefreshOutletBoard.plan(RefreshOutletIds.fdroid("official"), "official", 388)
        RefreshOutletBoard.resize(RefreshOutletIds.fdroid("official"), 39, 10)
        assertEquals(10, RefreshOutletBoard.snaps().single().done)
        assertEquals(39, RefreshOutletBoard.snaps().single().total)
        RefreshOutletBoard.fill(RefreshOutletIds.fdroid("official"))
        assertEquals(39, RefreshOutletBoard.snaps().single().done)
    }
}
