package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeftoverForgeParserTest {
    @Test
    fun gitlabProjects() {
        val json = """[{"path_with_namespace":"acme/app","name":"app","description":"org.acme.app","last_activity_at":"2024-01-02T00:00:00Z"}]"""
        val hit = LeftoverForgeParser.gitlabProjects(json).single()
        assertEquals("acme/app", hit.ownerRepo)
        assertEquals(ForgeHost.GitLab, hit.host)
    }

    @Test
    fun codebergRepos() {
        val json = """{"data":[{"full_name":"acme/app","description":"tools"}]}"""
        val hit = LeftoverForgeParser.codebergRepos(json).single()
        assertEquals("acme/app", hit.ownerRepo)
        assertEquals(ForgeHost.Codeberg, hit.host)
    }

    @Test
    fun blankSearchSkipped() {
        assertTrue(LeftoverForgeScan.first("  ", "  ", LeftoverSearchClient { _, _ -> GitHubSearchPage(200, "[]") }) == null)
    }
}
