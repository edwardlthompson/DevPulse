package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FdroidNotesApkTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun pageApkBecomesDirectUpdate() {
        val rec = FdroidAppRecord(
            "org.maps",
            1L,
            null,
            "izzy",
            apkName = "org.maps_2.apk",
        )
        FdroidNotes.remember(listOf(rec), setOf("org.maps"))
        val art = UpdateArtifactMemory.best("org.maps")
        assertEquals("https://apt.izzysoft.de/fdroid/repo/org.maps_2.apk", art?.downloadUrl)
        assertEquals(RemoteReleasedSource.Izzy, art?.source)
    }
}
