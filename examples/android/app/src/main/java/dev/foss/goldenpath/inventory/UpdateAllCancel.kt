package dev.foss.goldenpath.inventory

object UpdateAllCancel {
    @Volatile
    private var stop = false

    fun arm() {
        stop = false
    }

    fun request() {
        stop = true
    }

    fun requested(): Boolean = stop
}
