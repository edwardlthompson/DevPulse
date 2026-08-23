package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsPackTest {
    @Test
    fun roundTripSkipsInventoryKeys() {
        val raw = SettingsPack.encode(mapOf("theme" to "dark", "install" to "session"))
        assertEquals("dark", SettingsPack.decode(raw)["theme"])
        assertEquals(emptyMap<String, String>(), SettingsPack.decode(""))
    }
}
