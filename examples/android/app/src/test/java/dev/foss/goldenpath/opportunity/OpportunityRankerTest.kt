package dev.foss.goldenpath.opportunity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpportunityRankerTest {
    @Test
    fun groupsQuietAppsByCategory() {
        val gaps = OpportunityRanker.gaps(
            categoryByPackage = mapOf("a" to "tools", "b" to "tools", "c" to "maps"),
            quietPackages = setOf("a", "b"),
        )
        assertEquals("tools", gaps.first().category)
        assertEquals(2, gaps.first().quietCount)
        assertTrue(
            OpportunityRanker.selfPulseMatches(
                SelfPulseConfig("app.devpulse", "edwardlthompson/DevPulse"),
                "app.devpulse",
            ),
        )
    }
}
