package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAllRowFillTest {
    @Test
    fun downloadFillsOnFetchAndStaysFullAfter() {
        val fetch = snap(UpdateAllPhase.Fetch, received = 50, expected = 100)
        assertEquals(0.5f, UpdateAllRowFill.download(fetch).progress)
        assertFalse(UpdateAllRowFill.download(fetch).indeterminate)
        val ready = snap(UpdateAllPhase.Ready)
        assertEquals(1f, UpdateAllRowFill.download(ready).progress)
        val failDl = snap(UpdateAllPhase.Fail, failDownload = true)
        assertTrue(UpdateAllRowFill.download(failDl).error)
    }

    @Test
    fun installIsIndeterminateOnlyWhileApplying() {
        assertTrue(UpdateAllRowFill.install(snap(UpdateAllPhase.Apply)).indeterminate)
        assertEquals(0f, UpdateAllRowFill.install(snap(UpdateAllPhase.Ready)).progress)
        assertEquals(1f, UpdateAllRowFill.install(snap(UpdateAllPhase.Ok)).progress)
        val failIns = snap(UpdateAllPhase.Fail, failDownload = false)
        assertTrue(UpdateAllRowFill.install(failIns).error)
        assertEquals(0f, UpdateAllRowFill.install(snap(UpdateAllPhase.Fail, failDownload = true)).progress)
    }

    @Test
    fun fetchWithoutSizeIsIndeterminateDownload() {
        val bar = UpdateAllRowFill.download(snap(UpdateAllPhase.Fetch, expected = -1))
        assertTrue(bar.indeterminate)
    }

    private fun snap(
        phase: UpdateAllPhase,
        received: Long = 0,
        expected: Long = -1,
        failDownload: Boolean = false,
    ) = UpdateAllSnap("app.a", "A", RemoteReleasedSource.Play, phase, received, expected, failDownload)
}
