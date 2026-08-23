package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object RefreshWifiOnly {
    fun allow(wifiOnly: Boolean, unmetered: Boolean): Boolean = !wifiOnly || unmetered
}

class RefreshWifiPrefs(private val context: Context) {
    private val store get() = context.inventoryDataStore

    val enabled: Flow<Boolean> = store.booleanPref(KEY, false)

    suspend fun setEnabled(value: Boolean) {
        store.writeBoolean(KEY, value)
    }

    fun blockingEnabled(): Boolean = runBlocking { enabled.first() }

    companion object {
        private val KEY = booleanPreferencesKey("refresh_wifi_only")
    }
}
