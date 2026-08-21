package dev.foss.goldenpath.query

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanHistoryTest {
    @Test
    fun wentQuietNewReds() {
        val prev = mapOf("a" to "Green", "b" to "Red")
        val next = mapOf("a" to "Red", "b" to "Red", "c" to "Red")
        assertEquals(listOf("a"), ScanHistoryCodec.wentQuiet(prev, next))
    }

    @Test
    fun blankBadgeLineDropped() {
        assertEquals(null, ScanHistoryCodec.badgesLine(" ", "Red"))
        assertTrue(ScanHistoryCodec.parseBadges("\n\t\n").isEmpty())
    }
}
