package dev.foss.goldenpath.notify

import org.junit.Assert.assertFalse
import org.junit.Test

class UnifiedPushGateTest {
    @Test
    fun staysOff() {
        assertFalse(UnifiedPushGate.enabled)
    }
}
