package dev.foss.goldenpath.share

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenshotShareTest {
    @Test
    fun namesPng() {
        assertEquals("devpulse-1.png", ScreenshotShare.fileName(1L))
    }
}
