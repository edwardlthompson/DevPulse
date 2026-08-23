package dev.foss.goldenpath.inventory

object AppReprobe {
    fun forgetFetched(packageName: String) {
        val pick = RemoteReleaseMemory.byPackage[packageName] ?: return
        RemoteReleaseMemory.putAll(
            mapOf(
                packageName to pick.copy(offers = pick.offers.map { it.copy(fetchedAtMs = null) }),
            ),
        )
    }

    fun apply(packageName: String, offers: List<RemoteReleaseOffer>): RemoteReleasePick {
        val pick = RemoteReleaseRollup.from(offers)
        RemoteReleaseMemory.putAll(mapOf(packageName to pick))
        return RemoteReleaseMemory.byPackage.getValue(packageName)
    }
}
