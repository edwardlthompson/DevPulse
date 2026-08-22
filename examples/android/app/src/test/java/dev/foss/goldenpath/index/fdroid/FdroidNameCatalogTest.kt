package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidNameCatalogTest {
    @Test
    fun probeKeepsOnlyCatalogHits() {
        val catalog = FdroidNameCatalog.parse("org.fdroid.fdroid\norg.acme.app\n", "org.maps\n")
        assertTrue(catalog.loaded("official"))
        assertEquals(2, catalog.size("official"))
        assertEquals(1, catalog.size("izzy"))
        assertEquals(setOf("org.acme.app"), catalog.probe("official", setOf("org.acme.app", "com.play.only")))
        assertEquals(setOf("org.maps"), catalog.probe("izzy", setOf("org.maps", "com.play.only")))
        assertEquals(setOf("com.play.only"), catalog.probe("guardian", setOf("com.play.only")))
    }

    @Test
    fun emptyCatalogProbesEverything() {
        val catalog = FdroidNameCatalog.parse("", "")
        assertFalse(catalog.loaded("official"))
        assertEquals(setOf("a.b"), catalog.probe("official", setOf("a.b")))
    }
}
