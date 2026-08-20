package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidIndexMissesTest {
    @Test
    fun successfulIndexWithoutPackageIsKnownMiss() {
        val hits = listOf(FdroidAppRecord("app.other", 1L, null, "official"))
        val misses = FdroidIndexMisses.offers(
            wanted = setOf("com.instagram.android", "app.other"),
            okRepoIds = setOf("official", "izzy"),
            hits = hits,
        )
        val instagram = misses.getValue("com.instagram.android")
        assertEquals(2, instagram.size)
        assertTrue(instagram.all { !it.listed && it.known })
        assertEquals(
            setOf(RemoteReleasedSource.Fdroid, RemoteReleasedSource.Izzy),
            instagram.map { it.source }.toSet(),
        )
        assertTrue(misses.getValue("app.other").none { it.source == RemoteReleasedSource.Fdroid })
    }

    @Test
    fun failedRepoDoesNotCreateAMiss() {
        val misses = FdroidIndexMisses.offers(
            wanted = setOf("com.instagram.android"),
            okRepoIds = setOf("official"),
            hits = emptyList(),
        )
        assertTrue(misses.getValue("com.instagram.android").none { it.source == RemoteReleasedSource.Archive })
    }
}
