package dev.foss.goldenpath.inventory

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CacheWipeTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        ListingExtraBook.clear()
        UpdateArtifactMemory.clear()
        DumpChunkBook.clear()
    }

    @Test
    fun remotesDropsListingsAndIndexDir() {
        val dir = File.createTempFile("wipe", "dir").apply {
            delete()
            mkdirs()
        }
        File(dir, "remote_releases.json").writeText("{}")
        File(dir, "fdroid-index").mkdirs()
        File(dir, "dump_chunk_apkmirror.txt").writeText("x")
        ListingExtraBook.put("app.x", RemoteReleasedSource.Fdroid, ListingExtra(1L))
        CacheWipe.remotes(dir)
        assertTrue(ListingExtraBook.get("app.x", RemoteReleasedSource.Fdroid) == null)
        assertTrue(!File(dir, "remote_releases.json").exists())
        assertTrue(!File(dir, "fdroid-index").exists())
        assertTrue(!File(dir, "dump_chunk_apkmirror.txt").exists())
    }
}
