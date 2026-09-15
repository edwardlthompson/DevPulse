package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubSearchSkipTest {
    @Test
    fun blockedForThirtyDaysThenExpires() {
        val now = 1_720_000_000_000L
        val skip = GithubSearchSkip.remember(emptyMap(), "org.none.app", now)
        assertTrue(GithubSearchSkip.blocked("org.none.app", skip, now + 1_000L))
        assertFalse(GithubSearchSkip.blocked("org.none.app", skip, now + GithubSearchSkip.TTL_MS))
        assertFalse(GithubSearchSkip.blocked("org.other", skip, now))
    }

    @Test
    fun roundTripTsv() {
        val raw = GithubSearchSkip.encode(mapOf("org.none.app" to 9L))
        assertEquals(mapOf("org.none.app" to 9L), GithubSearchSkip.decode(raw))
    }
}
