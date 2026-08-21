package dev.foss.goldenpath.notify

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import dev.foss.goldenpath.inventory.booleanPref
import dev.foss.goldenpath.inventory.inventoryDataStore
import dev.foss.goldenpath.inventory.writeBoolean
import kotlinx.coroutines.flow.Flow

private val STALE_NOTIFY = booleanPreferencesKey("stale_crossing_notify")

class StaleNotifyPrefs(private val context: Context) {
    val enabled: Flow<Boolean> = context.inventoryDataStore.booleanPref(STALE_NOTIFY, false)

    suspend fun setEnabled(value: Boolean) {
        context.inventoryDataStore.writeBoolean(STALE_NOTIFY, value)
    }
}
