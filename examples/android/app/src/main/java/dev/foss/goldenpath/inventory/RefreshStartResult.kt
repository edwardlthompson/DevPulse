package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R

enum class RefreshStartResult {
    Started,
    AlreadyRunning,
    WifiBlocked,
}

object RefreshStartGate {
    fun decide(running: Boolean, wifiOnly: Boolean, unmetered: Boolean): RefreshStartResult = when {
        running -> RefreshStartResult.AlreadyRunning
        !RefreshWifiOnly.allow(wifiOnly, unmetered) -> RefreshStartResult.WifiBlocked
        else -> RefreshStartResult.Started
    }
}

object RefreshStartCopy {
    fun snackRes(result: RefreshStartResult): Int? = when (result) {
        RefreshStartResult.Started -> null
        RefreshStartResult.AlreadyRunning -> R.string.refresh_already_running
        RefreshStartResult.WifiBlocked -> R.string.refresh_wifi_only
    }
}
