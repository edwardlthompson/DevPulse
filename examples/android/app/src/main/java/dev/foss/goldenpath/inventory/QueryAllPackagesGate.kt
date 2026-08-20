package dev.foss.goldenpath.inventory

object QueryAllPackagesGate {
    const val ANDROID_11_SDK = 30

    fun mustExplain(sdkInt: Int): Boolean = sdkInt >= ANDROID_11_SDK

    fun canScan(acknowledged: Boolean, sdkInt: Int): Boolean =
        if (mustExplain(sdkInt)) acknowledged else true
}
