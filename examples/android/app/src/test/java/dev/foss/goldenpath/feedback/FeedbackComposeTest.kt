package dev.foss.goldenpath.feedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackComposeTest {
    @Test
    fun markdownKeepsKindAndBody() {
        val text = FeedbackCompose.markdown("bug", "list is empty", "stack")
        assertTrue(text.startsWith("## bug"))
        assertTrue(text.contains("list is empty"))
        assertTrue(text.contains("stack"))
    }

    @Test
    fun githubUrlIsComposeOnly() {
        val url = FeedbackGithub.issueUrl("empty list", "## bug\n\nhi", "bug")
        assertTrue(url.startsWith("https://github.com/edwardlthompson/DevPulse/issues/new"))
        assertTrue(url.contains("labels=bug"))
        assertFalse(url.contains("mailto:"))
    }

    @Test
    fun deepLinkMatchesFeedbackHostOnly() {
        assertTrue(FeedbackDeepLink.matches("devpulse", "feedback"))
        assertFalse(FeedbackDeepLink.matches("https", "github.com"))
        assertFalse(FeedbackDeepLink.matches("devpulse", "crash"))
    }
}
