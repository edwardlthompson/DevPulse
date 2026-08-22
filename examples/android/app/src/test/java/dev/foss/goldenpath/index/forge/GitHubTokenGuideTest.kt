package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubTokenGuideTest {
    @Test
    fun linkedStepsAreGithubHttps() {
        val linked = GitHubTokenGuide.steps.mapNotNull { it.url }
        assertEquals(
            listOf(
                GitHubTokenGuide.LOGIN_URL,
                GitHubTokenGuide.FINE_LIST_URL,
                GitHubTokenGuide.CREATE_URL,
            ),
            linked,
        )
        assertTrue(linked.all(GitHubTokenGuide::isGithubHttps))
        assertTrue(GitHubTokenGuide.isGithubHttps(GitHubTokenGuide.CLASSIC_URL))
    }

    @Test
    fun copyAndPasteHaveNoWebPage() {
        assertNull(GitHubTokenGuide.steps[3].url)
        assertNull(GitHubTokenGuide.steps[4].url)
    }
}
