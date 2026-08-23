package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshResumeTest {
    @Before
    fun reset() {
        RefreshSkip.reset()
        RefreshResume.clear()
        RefreshResume.persistDir = null
    }

    @Test
    fun leftoverDropsFinishedOutlets() {
        assertEquals(
            listOf("github"),
            RefreshResume.leftover(listOf("play", "github"), setOf("play")),
        )
    }

    @Test
    fun applyStopsOnlyPartialCheckpoint() {
        val dir = File.createTempFile("refresh", "dir").apply {
            delete()
            mkdirs()
        }
        RefreshResume.persistDir = dir
        RefreshResume.hydrate(setOf("play"))
        assertTrue(RefreshResume.apply(listOf("play", "github")))
        assertTrue(RefreshSkip.stopped("play"))
        assertFalse(RefreshSkip.stopped("github"))
    }

    @Test
    fun completeCheckpointClearsInsteadOfSkippingAll() {
        RefreshResume.hydrate(setOf("play", "github"))
        assertFalse(RefreshResume.apply(listOf("play", "github")))
        assertFalse(RefreshSkip.stopped("play"))
        assertTrue(RefreshResume.snapshot().isEmpty())
    }
}
