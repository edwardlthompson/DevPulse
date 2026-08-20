package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreSelectionTest {
    @Test
    fun sourcesFollowUserToggles() {
        val none = StoreSelection.sources(play = false, aptoide = false, forge = false, repoIds = emptySet())
        assertTrue(none.isEmpty())
        val selected = StoreSelection.sources(
            play = true,
            aptoide = false,
            forge = true,
            repoIds = setOf("official", "izzy"),
        )
        assertEquals(
            setOf(
                RemoteReleasedSource.Play,
                RemoteReleasedSource.Forge,
                RemoteReleasedSource.Fdroid,
                RemoteReleasedSource.Izzy,
            ),
            selected,
        )
        assertFalse(RemoteReleasedSource.Aptoide in selected)
        assertFalse(RemoteReleasedSource.Archive in selected)
    }

    @Test
    fun visibleHidesDisabledStores() {
        val listings = listOf(
            UpdateLink(RemoteReleasedSource.Play, listed = true, known = true),
            UpdateLink(RemoteReleasedSource.Aptoide, listed = false, known = true),
            UpdateLink(RemoteReleasedSource.Fdroid, listed = false, known = false),
        )
        val enabled = StoreSelection.sources(play = true, aptoide = false, forge = false, repoIds = setOf("official"))
        val shown = StoreSelection.visible(listings, enabled)
        assertEquals(listOf(RemoteReleasedSource.Play, RemoteReleasedSource.Fdroid), shown.map { it.source })
    }

    @Test
    fun rowsFillEveryEnabledStore() {
        val enabled = StoreSelection.sources(
            play = true,
            aptoide = true,
            forge = true,
            repoIds = setOf("official", "archive"),
        )
        val rows = StoreSelection.rows(
            listOf(UpdateLink(RemoteReleasedSource.Play, listed = true, known = true)),
            enabled,
        )
        assertEquals(enabled.size, rows.size)
        assertTrue(rows.any { it.source == RemoteReleasedSource.Forge && !it.known })
        assertTrue(rows.any { it.source == RemoteReleasedSource.Archive && !it.listed })
    }
}
