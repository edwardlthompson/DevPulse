package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubRepoParserTest {
    @Test
    fun parseReadsRepoNameAndPushDate() {
        val json = """
            {"items":[
              {"full_name":"TeamNewPipe/NewPipe","name":"NewPipe","pushed_at":"2024-01-15T12:00:00Z","archived":false}
            ]}
        """.trimIndent()
        val hit = GitHubRepoParser.parse(json).single()
        assertEquals("TeamNewPipe/NewPipe", hit.ownerRepo)
        assertEquals("NewPipe", hit.title)
        assertEquals(GitHubRepoParser.isoMs("2024-01-15T12:00:00Z"), hit.latestCommitMs)
        assertFalse(hit.archived)
    }

    @Test
    fun emptyItemsAndBadDateAreSafe() {
        assertEquals(emptyList<ForgeCandidate>(), GitHubRepoParser.parse("{}"))
        assertNull(GitHubRepoParser.isoMs(""))
        assertNull(GitHubRepoParser.isoMs("not-a-date"))
    }

    @Test
    fun queryUsesQuotedLabelWhenItHasSpaces() {
        assertEquals("NewPipe", GitHubSearchQuery.repositories("org.schabi.newpipe", "NewPipe"))
        assertEquals("\"Wipe Files\"", GitHubSearchQuery.repositories("uk.org.platitudes.wipefiles", "Wipe Files"))
        assertEquals("app.devpulse", GitHubSearchQuery.repositories("app.devpulse", "app.devpulse"))
        assertEquals("\"org.schabi.newpipe\"", GitHubSearchQuery.repositories("org.schabi.newpipe", "  "))
        assertEquals("Instagram", GitHubSearchQuery.repositories("  ", "Instagram"))
    }
}
