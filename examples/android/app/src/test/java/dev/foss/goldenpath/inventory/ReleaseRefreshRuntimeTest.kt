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
}
