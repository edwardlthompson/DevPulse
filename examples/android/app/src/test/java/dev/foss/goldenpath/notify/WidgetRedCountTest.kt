package dev.foss.goldenpath.notify

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetRedCountTest {
    @Test
    fun countsRedAndTreatsMissingAsZero() {
        assertEquals(2, WidgetRedCount.fromBadges(mapOf("a" to "Red", "b" to "Green", "c" to "red")))
        assertEquals(0, WidgetRedCount.fromBadges(emptyMap()))
    }
}
