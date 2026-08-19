package dev.foss.goldenpath.about

object UpdateChecker {
    fun parseVersion(tag: String): String? {
        val regex = Regex("v?(\\d+\\.\\d+\\.\\d+)")
        return regex.find(tag)?.groupValues?.get(1)
    }

    fun isNewer(current: String, latest: String): Boolean {
        fun parts(v: String) = v.split('.').map { it.toIntOrNull() ?: 0 }
        val a = parts(current)
        val b = parts(latest)
        for (i in 0..2) {
            val diff = (a.getOrElse(i) { 0 }) - (b.getOrElse(i) { 0 })
            if (diff != 0) return diff < 0
        }
        return false
    }
}
