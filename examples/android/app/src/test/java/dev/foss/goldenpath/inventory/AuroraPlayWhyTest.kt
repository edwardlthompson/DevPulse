package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraPlayWhyTest {
    @After
    fun releaseSession() {
        AuroraPlayLive.releaseSession()
    }

    @Test
    fun appNotPurchasedMapsToPlayPurchase() {
        assertEquals(InstallWhy.PlayPurchase, AuroraPlayWhy.of(RuntimeException("AppNotPurchased: null")))
        val typed = object : RuntimeException("denied") {}
        assertEquals(InstallWhy.NoFile, AuroraPlayWhy.of(typed))
        val nested = RuntimeException("wrap", RuntimeException("AppNotPurchased"))
        assertEquals(InstallWhy.PlayPurchase, AuroraPlayWhy.of(nested))
        assertEquals(InstallWhy.NoFile, AuroraPlayWhy.of(null))
    }

    @Test
    fun retryAfterEmptySkipsPurchaseAndHeldSession() {
        AuroraPlayLive.releaseSession()
        assertTrue(AuroraPlayLive.retryAfterEmpty(InstallWhy.NoFile))
        assertFalse(AuroraPlayLive.retryAfterEmpty(InstallWhy.PlayPurchase))
        AuroraPlayLive.holdSession()
        assertFalse(AuroraPlayLive.retryAfterEmpty(InstallWhy.NoFile))
    }
}
