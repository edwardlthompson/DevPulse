package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.inventoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "inventory_preferences",
)

internal fun DataStore<Preferences>.booleanPref(
    key: Preferences.Key<Boolean>,
    default: Boolean,
): Flow<Boolean> = data.map { prefs -> prefs[key] ?: default }

internal suspend fun DataStore<Preferences>.writeBoolean(
    key: Preferences.Key<Boolean>,
    value: Boolean,
) {
    edit { prefs -> prefs[key] = value }
}
