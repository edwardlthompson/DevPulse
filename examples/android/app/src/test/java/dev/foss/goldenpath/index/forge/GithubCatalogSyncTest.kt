package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.about.ProductUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubShippedCatalogTest {
    @Test
    fun parseSkipsBlankAndCommentLines() {
        val rows = GithubShippedCatalog.parse(
            "# generated\norg.fdroid.fdroid\tfdroid/fdroidclient\n\nbadline\napp.x\towner/repo\n",
        )
        assertEquals("fdroid/fdroidclient", rows["org.fdroid.fdroid"])
        assertEquals("owner/repo", rows["app.x"])
        assertEquals(2, rows.size)
    }
}

class GithubCatalogSyncTest {
    @Test
    fun localRowsWinOverPulled() {
        val merged = GithubCatalogSync.merge(
            mapOf("app.local" to "me/local", "app.both" to "me/keep"),
            mapOf("app.both" to "them/overwrite", "app.shipped" to "them/new"),
        )
        assertEquals("me/keep", merged["app.both"])
        assertEquals("me/local", merged["app.local"])
        assertEquals("them/new", merged["app.shipped"])
    }

    @Test
    fun notModifiedKeepsLocal() {
        val local = mapOf("app.x" to "owner/repo")
        val applied = GithubCatalogSync.apply(GithubCatalogPage(304, "", "\"v2\""), local, "\"v1\"")
        assertEquals(local, applied.rows)
        assertFalse(applied.saved)
        assertEquals("\"v1\"", applied.etag)
    }

    @Test
    fun httpOkMergesAndSaves() {
        val applied = GithubCatalogSync.apply(
            GithubCatalogPage(200, "app.x\towner/repo\n", "\"v2\""),
            mapOf("app.y" to "me/y"),
            null,
        )
        assertTrue(applied.saved)
        assertEquals("owner/repo", applied.rows["app.x"])
        assertEquals("me/y", applied.rows["app.y"])
        assertEquals("\"v2\"", applied.etag)
    }

    @Test
    fun errorDoesNotSave() {
        val local = mapOf("app.x" to "owner/repo")
        val applied = GithubCatalogSync.apply(GithubCatalogPage(500, "nope"), local, "\"v1\"")
        assertFalse(applied.saved)
        assertEquals(local, applied.rows)
    }

    @Test
    fun dailyDueMatchesProductUpdate() {
        assertTrue(GithubCatalogSync.due(null, 0L))
        assertFalse(GithubCatalogSync.due(0L, ProductUpdate.MS_DAY - 1))
        assertTrue(GithubCatalogSync.due(0L, ProductUpdate.MS_DAY))
    }
}
