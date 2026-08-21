package dev.foss.goldenpath.query

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.foss.goldenpath.inventory.inventoryDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val PINS = stringPreferencesKey("pinned_packages")

class PinPreferences(private val context: Context) {
    val pins: Flow<Set<String>> = context.inventoryDataStore.data.map { prefs ->
        decode(prefs[PINS])
    }

    suspend fun setPinned(packageName: String, pinned: Boolean) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        context.inventoryDataStore.edit { prefs ->
            val next = decode(prefs[PINS]).toMutableSet()
            if (pinned) next += pkg else next -= pkg
            prefs[PINS] = next.sorted().joinToString(",")
        }
    }

    companion object {
        fun decode(raw: String?): Set<String> =
            raw?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet().orEmpty()
    }
}
