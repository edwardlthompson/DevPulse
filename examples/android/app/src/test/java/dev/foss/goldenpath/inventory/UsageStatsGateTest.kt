package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageStatsGateTest {
    @Test
    fun usageStatsNeverRequiredForInventory() {
        assertFalse(UsageStatsGate.isRequiredForInventory())
    }

    @Test
    fun ranksOnlyWhenGranted() {
        assertFalse(UsageStatsGate.canRankByUsage(UsageStatsConsent.NotOffered))
        assertFalse(UsageStatsGate.canRankByUsage(UsageStatsConsent.WalkthroughSeen))
        assertFalse(UsageStatsGate.canRankByUsage(UsageStatsConsent.Declined))
        assertTrue(UsageStatsGate.canRankByUsage(UsageStatsConsent.Granted))
    }
}
