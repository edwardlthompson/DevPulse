package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DumpChunkBookTest {
    @Before
    fun reset() {
        DumpChunkBook.clear()
        DumpChunkBook.persistDir = null
    }

    @Test
    fun lastGoodChunkSurvivesHydrate() {
        val dir = File.createTempFile("dump", "dir").apply {
            delete()
            mkdirs()
        }
        DumpChunkBook.persistDir = dir
        DumpChunkBook.remember("apkmirror", """{"ok":true}""")
        DumpChunkBook.clear()
        DumpChunkBook.hydrate(dir)
        assertEquals("""{"ok":true}""", DumpChunkBook.last("apkmirror"))
    }
}
