package dev.foss.goldenpath.inventory

import java.io.File

object StorageRoom {
    const val RESERVE = 64L * 1024 * 1024

    fun enough(free: Long, need: Long = 0L, reserve: Long = RESERVE): Boolean =
        need >= 0L && free > need + reserve

    fun enough(dir: File, need: Long = 0L): Boolean = enough(dir.usableSpace, need)
}
