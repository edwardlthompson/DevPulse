package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GitHubSearchPaceTest {
    @Before
    fun reset() {
        GitHubSearchPace.reset()
    }

    @Test
    fun thirtyFitInOneMinuteThenWait() {
        var now = 1_000L
        var slept = 0L
        repeat(GitHubSearchPace.PER_MINUTE) {
            GitHubSearchPace.await(nowMs = { now }, sleepMs = { slept += it })
        }
        assertEquals(0L, slept)
        GitHubSearchPace.await(nowMs = { now }, sleepMs = { wait ->
            slept += wait
            now += wait
        })
        assertEquals(60_000L, slept)
    }
}
