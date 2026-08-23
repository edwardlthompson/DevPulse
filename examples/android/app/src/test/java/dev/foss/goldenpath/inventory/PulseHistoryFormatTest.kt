package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class PulseHistoryFormatTest {
    @Test
    fun newestFirstOrdersByTime() {
        val older = PulseHistoryRow(1_000L, "scan", 10L, 1)
        val newer = PulseHistoryRow(2_000L, "refresh", 20L, 2)
        assertEquals(listOf(newer, older), PulseHistoryFormat.newestFirst(listOf(older, newer)))
    }

    @Test
    fun refreshExtraSplitsOutletsFromLocationCount() {
        val view = PulseHistoryFormat.view(
            PulseHistoryRow(
                10L,
                "refresh",
                31_000L,
                388,
                "locations=12;play=24000;github=5000;fdroid:official=2000",
            ),
        )
        assertEquals(12, view.locations)
        assertEquals(
            listOf("play" to 24_000L, "github" to 5_000L, "fdroid:official" to 2_000L),
            view.outlets,
        )
        assertEquals(emptyList<Pair<String, String>>(), view.notes)
    }

    @Test
    fun scanAndUpdateKeepNotesNotOutlets() {
        val scan = PulseHistoryFormat.view(PulseHistoryRow(1L, "scan", 8L, 10, "red=2;unknown=1"))
        assertEquals(listOf("red" to "2", "unknown" to "1"), scan.notes)
        assertEquals(emptyList<Pair<String, Long>>(), scan.outlets)
        val update = PulseHistoryFormat.view(
            PulseHistoryRow(2L, "update", 9L, 3, "downloaded=3;failDl=0;failIns=1"),
        )
        assertEquals(3, update.notes.size)
        assertEquals(emptyList<Pair<String, Long>>(), update.outlets)
    }

    @Test
    fun blankAndUnknownBitsAreDropped() {
        val view = PulseHistoryFormat.view(PulseHistoryRow(1L, "refresh", 1L, 0, " ;=x;play=;foo"))
        assertEquals(emptyList<Pair<String, Long>>(), view.outlets)
        assertEquals(null, view.locations)
    }
}
