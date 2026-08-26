package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class WatchedRepoStoreTest {
    @Test
    fun unmatchedUrlPersistsWithoutInventoryRows() {
        assertEquals(null, WatchedRepoCodec.normalize(""))
        assertEquals(null, WatchedRepoCodec.normalize("onlyowner"))
        val store = FileWatchedRepoStore(File(createTempDirectory("watch").toFile(), "w.tsv"))
        store.add("ImranR98/Obtainium")
        store.add("bad")
        assertEquals(listOf("ImranR98/Obtainium"), store.load())
        store.remove("ImranR98/Obtainium")
        assertTrue(store.load().isEmpty())
    }
}
