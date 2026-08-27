package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidCustomIndexTest {
    @Test
    fun acceptsHttpsIndexJar() {
        assertTrue(FdroidCustomIndex.valid("https://f-droid.org/repo/index-v1.jar"))
        assertTrue(FdroidCustomIndex.valid(" https://apt.izzysoft.de/fdroid/repo/index-v1.json "))
    }

    @Test
    fun rejectsNonHttpsAndLocalHosts() {
        assertFalse(FdroidCustomIndex.valid(""))
        assertFalse(FdroidCustomIndex.valid("http://f-droid.org/repo/index-v1.jar"))
        assertFalse(FdroidCustomIndex.valid("https://127.0.0.1/repo/index-v1.jar"))
        assertFalse(FdroidCustomIndex.valid("https://localhost/repo/index-v1.jar"))
        assertFalse(FdroidCustomIndex.valid("https://example.com/repo/"))
    }
}
