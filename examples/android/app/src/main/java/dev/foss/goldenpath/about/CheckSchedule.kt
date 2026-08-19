package dev.foss.goldenpath.about

object CheckSchedule {
    private const val MS_DAY = 86_400_000L

    fun shouldCheck(interval: String, lastChecked: Long?, now: Long): Boolean {
        if (interval == "off") return false
        if (interval == "on_session") return lastChecked == null
        if (lastChecked == null) return true
        val elapsed = now - lastChecked
        return when (interval) {
            "daily" -> elapsed >= MS_DAY
            "weekly" -> elapsed >= 7 * MS_DAY
            "monthly" -> elapsed >= 30 * MS_DAY
            else -> false
        }
    }
}
