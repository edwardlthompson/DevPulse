package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingPlaySideloadTest {
    @Test
    fun skipsApkPureWhenPlayHeadersBroken() {
        assertFalse(ListingInstallLive.playSideload(true, InstallWhy.NoFile))
        assertTrue(ListingInstallLive.playSideload(false, InstallWhy.NoFile))
        assertFalse(ListingInstallLive.playSideload(false, InstallWhy.PlayPurchase))
    }
}
