package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseHistoryTest {
    @Test
    fun roundTripAndPrune() {
        val file = File.createTempFile("pulse", ".tsv")
        val old = PulseHistoryRow(1_000L, "refresh", 31_000L, 388, "play=24000")
        val keep = PulseHistoryRow(PulseHistory.RETAIN_MS + 2_000L, "update", 5_000L, 2, "installed=2")
        PulseHistory.append(file, old, nowMs = 2_000L)
        PulseHistory.append(file, keep, nowMs = PulseHistory.RETAIN_MS + 2_000L)
        val rows = PulseHistory.load(file)
        assertEquals(1, rows.size)
        assertEquals("update", rows.single().kind)
        assertEquals(2, rows.single().count)
        assertEquals("installed=2", rows.single().extra)
    }

    @Test
    fun dropsBlankKind() {
        assertEquals(null, PulseHistory.parse("1\t\t2\t3"))
        assertTrue(PulseHistory.parse("1\tscan\t2\t3\tred=1") != null)
    }
}
