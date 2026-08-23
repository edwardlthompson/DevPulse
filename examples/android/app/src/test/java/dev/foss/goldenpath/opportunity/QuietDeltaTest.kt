package dev.foss.goldenpath.opportunity

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class QuietDeltaTest {
    @Test
    fun countsNewQuietPackages() {
        assertEquals(1, QuietDelta.count(setOf("a"), setOf("a", "b")))
        val file = File.createTempFile("quiet", ".txt")
        QuietDelta.save(file, setOf("b", "a"))
        assertEquals(setOf("a", "b"), QuietDelta.load(file))
    }
}
