package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Test

class ArtifactFormatDetectorTest {
    @Test
    fun detectAndroidFormat_returnsApk() {
        assertEquals("apk", ArtifactFormatDetector.detectAndroidFormat())
    }
}
