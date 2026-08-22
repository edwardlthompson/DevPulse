package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WelcomePrefs(context: Context) {
    private val store = context.inventoryDataStore

    val seen: Flow<Boolean> = store.data.map { prefs ->
        prefs[WELCOME_SEEN] == true || prefs[QUERY_ALL_PACKAGES_ACK] == true
    }

    suspend fun markSeen() {
        store.writeBoolean(WELCOME_SEEN, true)
    }

    private companion object {
        val WELCOME_SEEN = booleanPreferencesKey("welcome_seen")
        val QUERY_ALL_PACKAGES_ACK = booleanPreferencesKey("query_all_packages_ack")
    }
}
