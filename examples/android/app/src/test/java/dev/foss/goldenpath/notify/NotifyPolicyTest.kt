package dev.foss.goldenpath.notify

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotifyPolicyTest {
    @Test
    fun emitsOnlyOnThresholdCross() {
        assertNull(NotifyPolicy.crossings(10, 20, "app.x"))
        assertEquals(180, NotifyPolicy.crossings(179, 180, "app.x")?.days)
        assertEquals(400, NotifyPolicy.crossings(364, 400, "app.x")?.days)
    }
}
