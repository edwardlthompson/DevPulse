package dev.foss.goldenpath.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLogicTest {
    @Test
    fun offIntervalDisablesUpdateCheck() {
        assertFalse(SettingsLogic.isUpdateCheckEnabled("off"))
    }

    @Test
    fun enablingRestoresWeeklyDefault() {
        assertEquals("weekly", SettingsLogic.intervalForToggle(true, "off"))
    }

    @Test
    fun disablingSetsOff() {
        assertEquals("off", SettingsLogic.intervalForToggle(false, "daily"))
    }

    @Test
    fun enablingPreservesCurrentInterval() {
        assertEquals("monthly", SettingsLogic.intervalForToggle(true, "monthly"))
    }
}
