package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class AirplaneCopyTest {
    @Test
    fun tagsCachedLine() {
        assertEquals("Play · 3:00", AirplaneCopy.tagged("Play · 3:00", airplane = false))
        assertEquals("Play · 3:00 · airplane", AirplaneCopy.tagged("Play · 3:00", airplane = true))
        assertEquals("", AirplaneCopy.tagged("", airplane = true))
    }
}
