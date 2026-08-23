package dev.foss.goldenpath.inventory

object ListingFit {
    fun sdkOk(minSdk: Int?, deviceSdk: Int): Boolean {
        val need = minSdk ?: return true
        if (need <= 0 || deviceSdk <= 0) return true
        return need <= deviceSdk
    }

    fun abiOk(natives: Set<String>, deviceAbis: Set<String>): Boolean {
        if (natives.isEmpty() || deviceAbis.isEmpty()) return true
        return natives.intersect(deviceAbis).isNotEmpty()
    }

    fun allow(link: UpdateLink, deviceSdk: Int, deviceAbis: Set<String>): Boolean =
        sdkOk(link.minSdk, deviceSdk) && abiOk(link.nativeCodes, deviceAbis)
}
