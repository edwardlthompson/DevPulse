package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ObtainiumImportModesTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun includeSettingsNoneWithNullSettingsImports() {
        val parsed = ObtainiumImport.parse(
            """{"schemaVersion":2,"apps":[{"id":"one.app","url":"https://github.com/One/Repo"}],"settings":null}""",
        )
        assertEquals(1, parsed.imported)
        assertEquals(0, parsed.skipped)
        assertEquals("One/Repo", GithubAdd.ownerRepo(parsed.rows.single().second))
    }

    @Test
    fun includeSettingsExcludeSecretsIgnoresSettingsObject() {
        val parsed = ObtainiumImport.parse(
            """{"schemaVersion":2,"apps":[{"id":"one.app","url":"https://github.com/One/Repo"}],"settings":{"theme":"dark","exportSettings":1}}""",
        )
        assertEquals(1, parsed.imported)
        assertEquals("one.app", parsed.rows.single().first)
    }

    @Test
    fun includeSettingsAllNeverPersistsCreds() {
        val json = """
            {"schemaVersion":2,"apps":[{"id":"one.app","url":"https://github.com/One/Repo"}],
             "settings":{"github-creds":"ghp_dummy_not_a_real_token","exportSettings":2}}
        """.trimIndent()
        val parsed = ObtainiumImport.parse(json)
        assertEquals(1, parsed.imported)
        val watched = FileWatchedRepoStore(tmp.newFile("watched-all.tsv"))
        ObtainiumImport.persist(
            parsed.rows,
            FilePastedRepoStore(tmp.newFile("pasted-all.tsv")),
            FileGithubVerifiedStore(tmp.newFile("verified-all.tsv")),
            watched,
        )
        assertTrue(!watched.load().toString().contains("ghp_"))
        assertEquals(listOf("One/Repo"), watched.load())
    }

    @Test
    fun bareAppsArrayStillImports() {
        val parsed = ObtainiumImport.parse(
            """[{"id":"one.app","url":"https://github.com/One/Repo"}]""",
        )
        assertEquals(1, parsed.imported)
        assertEquals("one.app", parsed.rows.single().first)
    }

    @Test
    fun schemaV2StringApkUrlsStillImports() {
        val json = """
            {"schemaVersion":2,"apps":[
              {"id":"com.beemdevelopment.aegis",
               "url":"https://github.com/beemdevelopment/Aegis",
               "apkUrls":"[[\"aegis.apk\",\"https://api.github.com/repos/beemdevelopment/Aegis/releases/assets/1\"]]"}
            ]}
        """.trimIndent()
        val parsed = ObtainiumImport.parse(json)
        assertEquals(1, parsed.imported)
        assertEquals("com.beemdevelopment.aegis", parsed.rows.single().first)
    }
}
