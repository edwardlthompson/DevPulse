package dev.foss.goldenpath.inventory

object RefreshTrace {
    var emit: (String) -> Unit = {}

    fun line(message: String) {
        synchronized(this) { emit(message) }
    }
}
