package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubTokenVerifyTest {
    private val store = object : ForgeTokenStore {
        var value: String? = null
        override fun getToken(): String? = value
        override fun setToken(token: String?) {
            value = token?.trim()?.ifEmpty { null }
        }
    }

    @Test
    fun acceptedSavesAndReadsHourlyLimit() {
        val client = GitHubTokenClient {
            Result.success(GitHubSearchPage(200, """{"resources":{"core":{"limit":5000,"remaining":4999}}}"""))
        }
        val check = GitHubTokenVerify.connect("  ghp_ok  ", client, store)
        assertEquals(GitHubTokenOutcome.Accepted, check.outcome)
        assertEquals(5000, check.hourlyLimit)
        assertEquals(4999, check.hourlyRemaining)
        assertEquals("ghp_ok", store.value)
    }

    @Test
    fun rejectedDoesNotSave() {
        val client = GitHubTokenClient { Result.success(GitHubSearchPage(401, """{"message":"Bad credentials"}""")) }
        val check = GitHubTokenVerify.connect("ghp_bad", client, store)
        assertEquals(GitHubTokenOutcome.Rejected, check.outcome)
        assertNull(store.value)
    }

    @Test
    fun forbiddenBadCredentialsDoesNotSave() {
        val client = GitHubTokenClient {
            Result.success(GitHubSearchPage(403, """{"message":"Bad credentials"}"""))
        }
        assertEquals(GitHubTokenOutcome.Rejected, GitHubTokenVerify.connect("ghp_bad", client, store).outcome)
        assertNull(store.value)
    }

    @Test
    fun blankDoesNotTouchStore() {
        store.value = "keep"
        val check = GitHubTokenVerify.connect("   ", GitHubTokenClient { error("no") }, store)
        assertEquals(GitHubTokenOutcome.Blank, check.outcome)
        assertEquals("keep", store.value)
    }

    @Test
    fun disconnectClears() {
        store.value = "ghp_ok"
        assertEquals(GitHubTokenOutcome.Cleared, GitHubTokenVerify.disconnect(store).outcome)
        assertNull(store.value)
    }
}
