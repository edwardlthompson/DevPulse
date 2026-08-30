package dev.foss.goldenpath.inventory

import java.io.File

object StorageRoom {
    const val RESERVE = 64L * 1024 * 1024

    fun enough(free: Long, need: Long = 0L, reserve: Long = RESERVE): Boolean =
        need >= 0L && free > need + reserve

    fun enough(dir: File, need: Long = 0L): Boolean {
        val free = bytes(dir)
        if (free <= 0L) return true
        return enough(free, need)
    }

    fun bytes(dir: File): Long {
        var best = 0L
        var at: File? = dir.absoluteFile
        var hops = 0
        while (at != null && hops < 6) {
            best = maxOf(best, at.usableSpace, at.freeSpace)
            at = at.parentFile
            hops += 1
        }
        return best
    }
}
