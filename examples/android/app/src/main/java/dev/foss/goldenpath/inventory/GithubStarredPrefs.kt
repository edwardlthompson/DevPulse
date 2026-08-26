package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow

class GithubStarredPrefs(private val context: Context) {
    private val store get() = context.inventoryDataStore

    val enabled: Flow<Boolean> = store.booleanPref(KEY, false)

    suspend fun setEnabled(value: Boolean) {
        store.writeBoolean(KEY, value)
    }

    companion object {
        private val KEY = booleanPreferencesKey("github_starred_scan")
    }
}
