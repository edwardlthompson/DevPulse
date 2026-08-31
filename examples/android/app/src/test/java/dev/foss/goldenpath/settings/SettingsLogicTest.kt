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
    fun overlayKeepsParentWhenChildOpen() {
        assertTrue(MenuPlace.composeParent(exclusiveSwap = false, childOpen = true))
        assertFalse(MenuPlace.composeParent(exclusiveSwap = true, childOpen = true))
        assertTrue(MenuPlace.composeParent(exclusiveSwap = false, childOpen = false))
    }

    @Test
    fun hubListsGlanceableSectionsThenAbout() {
        val rows = SettingsNav.hubRows()
        assertEquals(9, rows.size)
        assertEquals(SettingsPage.Appearance, rows[0].page)
        assertEquals(SettingsPage.Permissions, rows[1].page)
        assertEquals(SettingsPage.Inventory, rows[2].page)
        assertEquals(SettingsPage.Ideas, rows[3].page)
        assertEquals(SettingsPage.History, rows[4].page)
        assertEquals(SettingsPage.Updates, rows[5].page)
        assertEquals(SettingsPage.Sources, rows[6].page)
        assertEquals(SettingsPage.Stores, rows[7].page)
        assertEquals(null, rows[8].page)
        assertEquals(R.string.about_title, rows[8].titleRes)
    }
}
