package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshPaceFileTest {
    @Test
    fun roundTrip() {
        val file = File.createTempFile("pace", ".tsv")
        RefreshPaceFile.save(file, mapOf(RefreshOutletIds.PLAY to 40L))
        assertEquals(40L, RefreshPaceFile.load(file)[RefreshOutletIds.PLAY])
    }
}
