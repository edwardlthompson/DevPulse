package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ListingExtraBookTest {
    @Before
    fun reset() {
        ListingExtraBook.clear()
    }

    @Test
    fun remembersSizeAndAntiFeatures() {
        ListingExtraBook.put(
            "org.ver",
            RemoteReleasedSource.Fdroid,
            ListingExtra(4_200_000L, listOf("Tracking", "NonFreeNet")),
        )
        val extra = ListingExtraBook.get("org.ver", RemoteReleasedSource.Fdroid)
        assertEquals(4_200_000L, extra?.sizeBytes)
        assertEquals(listOf("Tracking", "NonFreeNet"), extra?.antiFeatures)
        assertNull(ListingExtraBook.get("org.ver", RemoteReleasedSource.Play))
    }
}
