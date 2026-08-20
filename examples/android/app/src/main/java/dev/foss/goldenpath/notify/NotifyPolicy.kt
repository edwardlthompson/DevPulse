package dev.foss.goldenpath.notify

data class StaleCrossing(
    val packageName: String,
    val days: Int,
)

object NotifyPolicy {
    const val SIX_MONTHS = 180
    const val ONE_YEAR = 365

    fun crossings(previousDays: Int?, currentDays: Int, packageName: String): StaleCrossing? {
        val prev = previousDays ?: return null
        val crossed = (prev < SIX_MONTHS && currentDays >= SIX_MONTHS) ||
            (prev < ONE_YEAR && currentDays >= ONE_YEAR)
        return if (crossed) StaleCrossing(packageName, currentDays) else null
    }
}

data class WidgetState(
    val redCount: Int,
)
