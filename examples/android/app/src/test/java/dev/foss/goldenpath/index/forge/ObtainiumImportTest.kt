package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ObtainiumImportTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun truncatedJsonImportsNothing() {
        val result = ObtainiumImport.parse("""{"apps":[{"id":"dev.app"""")
        assertEquals(0, result.imported)
        assertEquals(0, result.skipped)
        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun dummyPatNeverReachesRowsOrStores() {
        val json = """
            {"settings":{"githubPAT":"ghp_dummy_not_a_real_token"},
             "apps":[
               {"id":"dev.imranr.obtainium","url":"https://github.com/ImranR98/Obtainium",
                "additionalSettings":{"accessToken":"ghp_dummy_not_a_real_token"}},
               {"id":"org.unknown","url":"https://uptodown.com/foo"}
             ]}
        """.trimIndent()
        val parsed = ObtainiumImport.parse(json)
        assertEquals(1, parsed.imported)
        assertEquals(1, parsed.skipped)
        assertEquals("dev.imranr.obtainium", parsed.rows.single().first)
        assertTrue(parsed.rows.none { it.second.contains("ghp_") })
        val pasted = FilePastedRepoStore(tmp.newFile("pasted.tsv"))
        val verified = FileGithubVerifiedStore(tmp.newFile("verified.tsv"))
        val watched = FileWatchedRepoStore(tmp.newFile("watched.tsv"))
        assertEquals(1, ObtainiumImport.persist(parsed.rows, pasted, verified, watched))
        val dump = pasted.load().toString() + verified.load().toString()
        assertTrue(!dump.contains("ghp_"))
        assertEquals("ImranR98/Obtainium", verified.load()["dev.imranr.obtainium"])
    }
}
