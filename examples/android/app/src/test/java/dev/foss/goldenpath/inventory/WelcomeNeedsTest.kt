package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeNeedsTest {
    @Test
    fun continueWaitsForRequiredGrants() {
        val blocked = WelcomeNeeds.rows(
            appsAccepted = false,
            notifyGranted = true,
            installGranted = true,
            usageGranted = false,
            sdkInt = 34,
        )
        assertFalse(WelcomeNeeds.ready(blocked))
        val ready = WelcomeNeeds.rows(
            appsAccepted = true,
            notifyGranted = true,
            installGranted = true,
            usageGranted = false,
            sdkInt = 34,
        )
        assertTrue(WelcomeNeeds.ready(ready))
    }

    @Test
    fun notificationsOptionalBelow33() {
        val rows = WelcomeNeeds.rows(
            appsAccepted = true,
            notifyGranted = false,
            installGranted = true,
            usageGranted = false,
            sdkInt = 32,
        )
        assertTrue(WelcomeNeeds.ready(rows))
    }

    @Test
    fun welcomeOnlyUntilSeenThenSplashOrInventory() {
        assertEquals(WelcomeHome.Splash, WelcomeNeeds.home(null, canScan = false))
        assertEquals(WelcomeHome.Welcome, WelcomeNeeds.home(false, canScan = false))
        assertEquals(WelcomeHome.Splash, WelcomeNeeds.home(true, canScan = false))
        assertEquals(WelcomeHome.Inventory, WelcomeNeeds.home(true, canScan = true))
        assertTrue(WelcomeNeeds.seen(welcomeSeen = true, acknowledged = false))
        assertTrue(WelcomeNeeds.seen(welcomeSeen = false, acknowledged = true))
        assertFalse(WelcomeNeeds.seen(welcomeSeen = false, acknowledged = false))
    }
}
