package dev.foss.goldenpath.index.aptoide

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AptoideCatalogTest {
    @Before
    fun reset() {
        AptoideCatalog.reset()
    }

    @Test
    fun gamesPickChangesStoreName() {
        assertEquals(AptoideCatalog.STORE, AptoideCatalog.storeName())
        AptoideCatalog.pick(games = true)
        assertEquals(AptoideCatalog.GAMES, AptoideCatalog.storeName())
        AptoideCatalog.pick(games = false)
        assertEquals(AptoideCatalog.STORE, AptoideCatalog.storeName())
    }
}
