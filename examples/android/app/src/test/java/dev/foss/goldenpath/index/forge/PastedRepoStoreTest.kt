package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class PastedRepoStoreTest {
    @Test
    fun ignoresBlankAndKeepsHttps() {
        assertEquals(null, PastedRepoCodec.normalize("org.app", "  "))
        assertEquals(null, PastedRepoCodec.normalize("org.app", "ftp://example.com/r"))
        val store = FilePastedRepoStore(File(createTempDirectory("paste").toFile(), "p.tsv"))
        store.put("org.app", "https://github.com/acme/app")
        assertEquals("https://github.com/acme/app", store.load()["org.app"])
        val hints = PastedRepoCodec.hints(store.load())
        assertEquals("acme/app", hints.getValue("org.app").ownerRepo)
        assertTrue(PastedRepoCodec.hints(mapOf("org.app" to "https://example.com")).isEmpty())
    }
}
