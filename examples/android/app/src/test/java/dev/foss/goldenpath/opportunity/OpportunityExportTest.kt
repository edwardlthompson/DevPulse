package dev.foss.goldenpath.opportunity

import dev.foss.goldenpath.inventory.UsageSnapshot
import dev.foss.goldenpath.inventory.sampleApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpportunityExportTest {
    @Test
    fun ranksByUsageAndRendersCsvJson() {
        val quiet = listOf(sampleApp("org.a", "Alpha"), sampleApp("org.b", "Beta"))
        val titles = OpportunityExport.quietTitles(
            quiet,
            listOf(UsageSnapshot("org.b", 9L, 1L), UsageSnapshot("org.a", 1L, 1L)),
        )
        assertEquals(listOf("Beta", "Alpha"), titles)
        val gaps = listOf(CategoryGap("Maps", 2))
        val csv = OpportunityExport.csv(titles, gaps)
        assertTrue(csv.contains("quiet,Beta"))
        assertTrue(csv.contains("gap,Maps,2"))
        assertTrue(OpportunityExport.json(titles, gaps).contains("\"quietCount\":2"))
    }
}
