package dev.foss.goldenpath.inventory

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object MeteredNet {
    fun needsConfirm(metered: Boolean, jobs: Int): Boolean = metered && jobs > 0

    fun metered(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
}
