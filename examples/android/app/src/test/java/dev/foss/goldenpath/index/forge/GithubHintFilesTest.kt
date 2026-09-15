package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GithubHintFilesTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun aliasLookupUsesVerifiedAndPasted() {
        val dir = tmp.newFolder("files")
        FileGithubVerifiedStore(java.io.File(dir, "github_verified.tsv"))
            .put("dev.imranr.obtainium.fdroid", "ImranR98/Obtainium")
        FilePastedRepoStore(java.io.File(dir, "pasted_repos.tsv"))
            .put("org.exact", "https://github.com/paste/repo")
        val aliased = GithubHintFiles.hint(dir, "dev.imranr.obtainium")
        assertEquals("ImranR98/Obtainium", aliased?.ownerRepo)
        assertNull(aliased?.versionName)
        assertEquals("paste/repo", GithubHintFiles.hint(dir, "org.exact")?.ownerRepo)
        assertNull(GithubHintFiles.hint(dir, "org.mozilla.firefox"))
        assertTrue(GithubHintFiles.library(dir).containsKey("dev.imranr.obtainium.fdroid"))
    }

    @Test
    fun localVerifiedWinsOverShipped() {
        val dir = tmp.newFolder("overlay")
        FileGithubVerifiedStore(java.io.File(dir, "github_verified.tsv"))
            .put("org.app", "me/keep")
        val hints = GithubHintFiles.load(dir, mapOf("org.app" to "ship/old", "org.other" to "ship/new"))
        assertEquals("me/keep", hints["org.app"]?.ownerRepo)
        assertEquals("ship/new", hints["org.other"]?.ownerRepo)
    }
}
