package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshWifiOnlyTest {
    @Test
    fun allowWhenOffOrUnmetered() {
        assertTrue(RefreshWifiOnly.allow(wifiOnly = false, unmetered = false))
        assertTrue(RefreshWifiOnly.allow(wifiOnly = true, unmetered = true))
        assertFalse(RefreshWifiOnly.allow(wifiOnly = true, unmetered = false))
    }
}
