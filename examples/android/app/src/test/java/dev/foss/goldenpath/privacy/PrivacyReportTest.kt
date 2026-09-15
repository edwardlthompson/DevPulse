package dev.foss.goldenpath.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyReportTest {
    @Test
    fun reportStaysLocal() {
        val text = PrivacyReport.text()
        assertTrue(text.contains("local-only"))
        assertFalse(text.contains("dsn", ignoreCase = true))
        assertFalse(text.contains("http"))
    }
}
