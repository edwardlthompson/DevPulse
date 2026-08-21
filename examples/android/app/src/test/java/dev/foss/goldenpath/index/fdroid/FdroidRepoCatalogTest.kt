package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FdroidRepoCatalogTest {
    @Test
    fun archiveUsesOfficialIndexUrl() {
        val archive = FdroidRepoCatalog.defaults().single { it.id == "archive" }
        assertEquals(FdroidRepoKind.Archive, archive.kind)
        assertEquals("https://f-droid.org/archive/index-v1.jar", archive.indexUrl)
    }

    @Test
    fun vendorReposHaveInAppApkBases() {
        val ids = listOf("microg", "newpipe", "divest", "kde", "cromite", "iode")
        val catalog = FdroidRepoCatalog.defaults()
        ids.forEach { id ->
            val repo = catalog.single { it.id == id }
            assertEquals(FdroidRepoKind.Vendor, repo.kind)
            assertTrue(repo.indexUrl.startsWith("https://"))
            assertTrue(repo.indexUrl.endsWith("/index-v1.jar"))
            assertEquals(
                repo.indexUrl.removeSuffix("index-v1.jar"),
                FdroidRepoCatalog.apkBase(id),
            )
            assertTrue(FdroidApkUrl.of(id, "app_1.apk")!!.startsWith("https://"))
        }
    }
}
