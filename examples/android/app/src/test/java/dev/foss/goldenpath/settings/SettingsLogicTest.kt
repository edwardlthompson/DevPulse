package dev.foss.goldenpath.settings

import dev.foss.goldenpath.R
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

    @Test
    fun hubListsGlanceableSectionsThenAbout() {
        val rows = SettingsNav.hubRows()
        assertEquals(7, rows.size)
        assertEquals(SettingsPage.Appearance, rows[0].page)
        assertEquals(SettingsPage.Permissions, rows[1].page)
        assertEquals(SettingsPage.Stores, rows[5].page)
        assertEquals(null, rows[6].page)
        assertEquals(R.string.about_title, rows[6].titleRes)
    }
}
