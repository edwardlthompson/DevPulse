package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateNotesTest {
    @org.junit.Before
    fun reset() {
        UpdateNotesMemory.clear()
    }

    @Test
    fun memoryStoresNonBlankNotes() {
        UpdateNotesMemory.put("app.one", UpdateNotes("  ", RemoteReleasedSource.Fdroid))
        assertNull(UpdateNotesMemory.get("app.one"))
        UpdateNotesMemory.put("app.one", UpdateNotes("Crash fix", RemoteReleasedSource.Forge))
        assertEquals("Crash fix", UpdateNotesMemory.get("app.one")?.text)
        assertEquals(RemoteReleasedSource.Forge, UpdateNotesMemory.get("app.one")?.source)
    }

    @Test
    fun putIfAbsentKeepsFirst() {
        UpdateNotesMemory.putIfAbsent("app.one", UpdateNotes("Fdroid", RemoteReleasedSource.Fdroid))
        UpdateNotesMemory.putIfAbsent("app.one", UpdateNotes("Izzy", RemoteReleasedSource.Izzy))
        assertEquals("Fdroid", UpdateNotesMemory.get("app.one")?.text)
        UpdateNotesMemory.put("app.one", UpdateNotes("GitHub", RemoteReleasedSource.Forge))
        assertEquals("GitHub", UpdateNotesMemory.get("app.one")?.text)
    }
}
