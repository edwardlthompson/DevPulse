package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Test

class FdroidRepoCatalogTest {
    @Test
    fun archiveUsesOfficialIndexUrl() {
        val archive = FdroidRepoCatalog.defaults().single { it.id == "archive" }
        assertEquals(FdroidRepoKind.Archive, archive.kind)
        assertEquals("https://f-droid.org/archive/index-v1.jar", archive.indexUrl)
    }
}
