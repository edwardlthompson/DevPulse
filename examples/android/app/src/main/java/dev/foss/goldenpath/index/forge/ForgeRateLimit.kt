package dev.foss.goldenpath.index.forge

import java.util.concurrent.atomic.AtomicInteger

object ForgeRateLimit {
    const val GITHUB_SKIP_AFTER = 3
    const val LEFTOVER_SKIP_AFTER = 3

    private val githubHits = AtomicInteger(0)
    private val leftoverTimeouts = AtomicInteger(0)

    fun reset() {
        githubHits.set(0)
        leftoverTimeouts.set(0)
    }

    fun noteGithub(statusCode: Int) {
        if (statusCode == 403 || statusCode == 429) githubHits.incrementAndGet()
    }

    fun noteLeftoverTimeout() {
        leftoverTimeouts.incrementAndGet()
    }

    fun githubBlocked(): Boolean = githubHits.get() >= GITHUB_SKIP_AFTER

    fun leftoverBlocked(): Boolean = leftoverTimeouts.get() >= LEFTOVER_SKIP_AFTER
}
