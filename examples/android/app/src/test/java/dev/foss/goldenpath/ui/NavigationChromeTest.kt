package dev.foss.goldenpath.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationChromeTest {
    @Test
    fun homeIsSettingsOnly() {
        assertFalse(NavigationChrome.bottomNav)
        assertTrue(NavigationChrome.homeSettingsOnly)
    }
}
