package dev.foss.goldenpath.inventory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class RefreshHostGate(
    playSlots: Int = PLAY,
    aptoideSlots: Int = APTOIDE,
    githubSlots: Int = GITHUB,
) {
    private val play = Semaphore(playSlots.coerceAtLeast(1))
    private val aptoide = Semaphore(aptoideSlots.coerceAtLeast(1))
    private val github = Semaphore(githubSlots.coerceAtLeast(1))

    fun <T> play(block: () -> T): T = hold(play, block)
    fun <T> aptoide(block: () -> T): T = hold(aptoide, block)
    fun <T> github(block: () -> T): T = hold(github, block)

    private fun <T> hold(sem: Semaphore, block: () -> T): T {
        sem.acquire()
        return try {
            block()
        } finally {
            sem.release()
        }
    }

    companion object {
        const val PLAY = 8
        const val APTOIDE = 8
        const val GITHUB = 4
        const val REPOS = 5
        const val APPS = 12
    }
}

object ReleaseRefreshParallel {
    fun <T, R> map(
        items: List<T>,
        workers: Int,
        executor: ExecutorService? = null,
        block: (T) -> R,
    ): List<R> {
        if (items.isEmpty()) return emptyList()
        val n = workers.coerceAtLeast(1)
        if (executor == null && (n == 1 || items.size == 1)) return items.map(block)
        val owned = executor == null
        val pool = executor ?: Executors.newFixedThreadPool(n.coerceAtMost(items.size))
        val slots = Semaphore(n)
        return try {
            items.map { item ->
                pool.submit<R> {
                    slots.acquire()
                    try {
                        block(item)
                    } finally {
                        slots.release()
                    }
                }
            }.map { it.get() }
        } finally {
            if (owned) {
                pool.shutdown()
                pool.awaitTermination(30, TimeUnit.MINUTES)
            }
        }
    }
}
