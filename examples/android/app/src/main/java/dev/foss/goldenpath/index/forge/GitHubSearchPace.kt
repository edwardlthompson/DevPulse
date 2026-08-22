package dev.foss.goldenpath.index.forge

object GitHubSearchPace {
    const val PER_MINUTE = 30
    private const val WINDOW_MS = 60_000L
    private val lock = Any()
    private val stamps = ArrayDeque<Long>()

    fun reset() {
        synchronized(lock) { stamps.clear() }
    }

    fun await(
        nowMs: () -> Long = { System.currentTimeMillis() },
        sleepMs: (Long) -> Unit = { ms -> if (ms > 0) Thread.sleep(ms) },
    ) {
        while (true) {
            val wait = synchronized(lock) {
                val now = nowMs()
                while (stamps.isNotEmpty() && now - stamps.first() >= WINDOW_MS) {
                    stamps.removeFirst()
                }
                if (stamps.size < PER_MINUTE) {
                    stamps.addLast(now)
                    0L
                } else {
                    (WINDOW_MS - (now - stamps.first())).coerceAtLeast(1L)
                }
            }
            if (wait <= 0L) return
            sleepMs(wait)
        }
    }
}
