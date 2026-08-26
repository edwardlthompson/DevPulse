package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GithubAddTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun ownerRepoRejectsEmptyFtpAndOrgs() {
        assertNull(GithubAdd.ownerRepo(""))
        assertNull(GithubAdd.ownerRepo("   "))
        assertNull(GithubAdd.ownerRepo("ftp://github.com/a/b"))
        assertNull(GithubAdd.ownerRepo("https://github.com/orgs/foo"))
        assertEquals("ImranR98/Obtainium", GithubAdd.ownerRepo("https://github.com/ImranR98/Obtainium"))
    }

    @Test
    fun persistWithoutMatchWatchesRepo() {
        val pasted = FilePastedRepoStore(tmp.newFile("pasted.tsv"))
        val verified = FileGithubVerifiedStore(tmp.newFile("verified.tsv"))
        val watched = FileWatchedRepoStore(tmp.newFile("watched.tsv"))
        val bound = GithubAdd.persist("ImranR98/Obtainium", emptyList(), pasted, verified, watched)
        assertTrue(bound.isEmpty())
        assertEquals(listOf("ImranR98/Obtainium"), watched.load())
        assertTrue(pasted.load().isEmpty())
        assertTrue(verified.load().isEmpty())
    }
}
