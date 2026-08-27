package dev.foss.goldenpath.index.forge

import java.io.ByteArrayInputStream
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
    fun nestedArraysImportEveryGithubWatch() {
        val json = """
            {"apps":[
              {"id":"one.app","url":"https://github.com/One/Repo",
               "categories":["x"],"apkUrls":[["a.apk","https://example/a.apk"]]},
              {"id":"two.app","url":"https://github.com/Two/Repo",
               "additionalSettings":"{\"apkFilterRegEx\":\"[.]apk$\",\"urls\":[1,2]}"},
              {"id":"skip.me","url":"https://gitlab.com/org/app"}
            ]}
        """.trimIndent()
        val parsed = ObtainiumImport.parse(json)
        assertEquals(2, parsed.imported)
        assertEquals(1, parsed.skipped)
        val pasted = FilePastedRepoStore(tmp.newFile("pasted.tsv"))
        val verified = FileGithubVerifiedStore(tmp.newFile("verified.tsv"))
        val watched = FileWatchedRepoStore(tmp.newFile("watched.tsv"))
        assertEquals(2, ObtainiumImport.persist(parsed.rows, pasted, verified, watched))
        assertEquals(listOf("One/Repo", "Two/Repo"), watched.load())
        assertEquals("One/Repo", verified.load()["one.app"])
        assertEquals("Two/Repo", verified.load()["two.app"])
    }

    @Test
    fun githubUrlWithoutPackageIdStillWatched() {
        val parsed = ObtainiumImport.parse(
            """{"apps":[{"id":"","url":"https://github.com/Watch/Me"}]}""",
        )
        assertEquals(1, parsed.imported)
        val watched = FileWatchedRepoStore(tmp.newFile("watched.tsv"))
        assertEquals(
            1,
            ObtainiumImport.persist(
                parsed.rows,
                FilePastedRepoStore(tmp.newFile("pasted.tsv")),
                FileGithubVerifiedStore(tmp.newFile("verified.tsv")),
                watched,
            ),
        )
        assertEquals(listOf("Watch/Me"), watched.load())
    }

    @Test
    fun sandboxNameRejectsPaths() {
        assertNull(ObtainiumImport.sandboxName("../x.json"))
        assertNull(ObtainiumImport.sandboxName("/sdcard/Obt/x.json"))
        assertNull(ObtainiumImport.sandboxName("notes.txt"))
        assertEquals("obtainium-import.json", ObtainiumImport.sandboxName("obtainium-import.json"))
    }

    @Test
    fun oversizeBackupIsRejected() {
        val bytes = ByteArray(ObtainiumImport.MAX_BYTES + 1) { '{'.code.toByte() }
        assertNull(ObtainiumImport.readUtf8(ByteArrayInputStream(bytes)))
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
        val dump = pasted.load().toString() + verified.load().toString() + watched.load().toString()
        assertTrue(!dump.contains("ghp_"))
        assertEquals("ImranR98/Obtainium", verified.load()["dev.imranr.obtainium"])
        assertEquals(listOf("ImranR98/Obtainium"), watched.load())
    }
}
