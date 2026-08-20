package dev.foss.goldenpath.inventory

enum class UsageStatsConsent {
    NotOffered,
    WalkthroughSeen,
    Granted,
    Declined,
}

object UsageStatsGate {
    fun isRequiredForInventory(): Boolean = false

    fun canRankByUsage(consent: UsageStatsConsent): Boolean =
        consent == UsageStatsConsent.Granted
}
