package dev.foss.goldenpath.inventory

import java.util.concurrent.Future

/** Wait for any in-flight download or install instead of a whole wave. */
internal object UpdateAllSlots {
    fun waitDone(futures: List<Future<*>>) {
        if (futures.isEmpty()) return
        while (futures.none { it.isDone }) Thread.sleep(20)
        runCatching { futures.first { it.isDone }.get() }
    }
}
