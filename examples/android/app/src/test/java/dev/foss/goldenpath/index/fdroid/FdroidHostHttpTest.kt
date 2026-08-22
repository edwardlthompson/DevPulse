package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidHostHttpTest {
    @Test
    fun skipsPageWhenApiMisses() {
        var pages = 0
        val http = FdroidHostHttp(
            api = FdroidPackageClient { Result.failure(IllegalStateException("miss")) },
            pages = FdroidPageClient { pages += 1; Result.success("<html>") },
            workers = 1,
        )
        val official = FdroidRepoCatalog.defaults().first { it.id == "official" }
        assertTrue(http.resolve(official, setOf("org.none")).isEmpty())
        assertEquals(0, pages)
    }
}
