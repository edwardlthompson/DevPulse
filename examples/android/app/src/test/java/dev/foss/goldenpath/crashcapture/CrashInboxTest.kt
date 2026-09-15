package dev.foss.goldenpath.crashcapture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashInboxTest {
    @Test
    fun exampleStaysOff() {
        assertFalse(CrashInbox.enabled("""{"enabled": false, "provider": "none", "dsn": ""}"""))
        assertTrue(CrashInbox.enabled("""{"enabled": true}"""))
    }
}
