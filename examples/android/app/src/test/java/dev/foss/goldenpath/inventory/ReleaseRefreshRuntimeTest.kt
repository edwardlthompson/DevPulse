package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReleaseRefreshRuntimeTest {
    @Before
    fun reset() {
        ReleaseRefreshRuntime.reset()
    }

    @Test
    fun tryBeginOnlyOnceUntilFinish() {
        assertTrue(ReleaseRefreshRuntime.tryBegin())
        assertTrue(ReleaseRefreshRuntime.running.value)
        assertFalse(ReleaseRefreshRuntime.tryBegin())
        ReleaseRefreshRuntime.setProgress(RefreshProgress(2, 4))
        assertEquals(2, ReleaseRefreshRuntime.progress.value.done)
        ReleaseRefreshRuntime.finish()
        assertFalse(ReleaseRefreshRuntime.running.value)
        assertTrue(ReleaseRefreshRuntime.tryBegin())
    }

    @Test
    fun pauseHoldsUntilResume() {
        assertTrue(ReleaseRefreshRuntime.tryBegin())
        ReleaseRefreshRuntime.pause()
        assertTrue(ReleaseRefreshRuntime.paused.value)
        ReleaseRefreshRuntime.resume()
        assertFalse(ReleaseRefreshRuntime.paused.value)
        ReleaseRefreshRuntime.finish()
        ReleaseRefreshRuntime.pause()
        assertFalse(ReleaseRefreshRuntime.paused.value)
    }
}
