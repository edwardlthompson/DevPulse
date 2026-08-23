package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshHostBackoff
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

    fun noteGithub(statusCode: Int, retryAfterSec: Long? = null) {
        if (statusCode != 403 && statusCode != 429) return
        val hits = githubHits.incrementAndGet()
        ForgeBackoff.nextDelayMs(statusCode, hits, retryAfterSec)?.let { delay ->
            val wait = if (hits >= GITHUB_SKIP_AFTER) maxOf(300_000L, delay) else delay
            RefreshHostBackoff.note("github", wait)
        }
    }

    fun noteLeftoverTimeout() {
        val hits = leftoverTimeouts.incrementAndGet()
        RefreshHostBackoff.note("leftover", if (hits >= LEFTOVER_SKIP_AFTER) 300_000L else 5_000L)
    }

    fun githubBlocked(): Boolean = githubHits.get() >= GITHUB_SKIP_AFTER

    fun leftoverBlocked(): Boolean = leftoverTimeouts.get() >= LEFTOVER_SKIP_AFTER

    fun leftoverLeft(): Int = (LEFTOVER_SKIP_AFTER - leftoverTimeouts.get()).coerceAtLeast(0)
}
