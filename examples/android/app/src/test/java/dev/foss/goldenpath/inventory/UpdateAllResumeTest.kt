package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAllResumeTest {
    @Test
    fun leftoverDropsSettledAndEmptyGroups() {
        val open = listOf(
            "app.a" to listOf(UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null)),
            "app.b" to listOf(UpdateAllJob("app.b", "B", RemoteReleasedSource.Izzy, null)),
            "app.c" to emptyList(),
        )
        assertEquals(listOf("app.b"), UpdateAllResume.leftover(open, setOf("app.a")))
    }

    @Test
    fun checkpointWritesAndClears() {
        val dir = File.createTempFile("resume", "dir").apply { delete(); mkdirs() }
        val open = listOf(
            "app.a" to listOf(UpdateAllJob("app.a", "A", RemoteReleasedSource.Fdroid, null)),
        )
        UpdateAllResume.checkpoint(dir, open, emptySet())
        assertEquals(listOf("app.a"), UpdateAllResume.load(dir))
        UpdateAllResume.checkpoint(dir, open, setOf("app.a"))
        assertTrue(UpdateAllResume.load(dir).isEmpty())
    }
}
